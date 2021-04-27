package eng.airCompanyFleetDownloader;

import eng.eSystem.EStringBuilder;
import eng.eSystem.collections.*;
import eng.eSystem.eXml.EXmlException;
import eng.eSystem.eXml.XDocument;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.ApplicationException;
import eng.eSystem.utilites.ExceptionUtils;
import eng.eXmlSerialization.StoreTypeMode;
import eng.eXmlSerialization.XmlSerializer;
import eng.eXmlSerialization.XmlSettings;
import eng.eXmlSerialization.common.Log;
import eng.eXmlSerialization.meta.XmlRule;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class Program {

  private static final boolean USE_XML_LOG = false;
  private static final int DOWNLOAD_DELAY = 5000;
  private static final Path path = Paths.get("C:\\temp\\airlines");
  private static EMap<String, String> typeMap = new EMap<>();

  public static void main(String[] params) throws EXmlException {
    System.out.println("Target dir " + path.toString());
    try {
      java.nio.file.Files.createDirectory(path);
    } catch (IOException e) {
      System.out.println("Warning: Output directory already exists. Items may be overwritten.");
    }

    //tmp();
    //downloadAirlines();
    //downloadAirlinesFleets();
    convertToFleets();
  }

  private static void convertToFleets() {
    System.out.println("Phase 3 - converting to fleets xml file");

    XmlSerializer ser = getSerializer();
    IList<AirlineInfo> lst = deserializeFromDocument(ser, getAirlinesListFile().toString(), EList.class);
    System.out.println("\t found " + lst.count() + " airlines");
    lst = lst.where(q -> q.isDecoded());
    System.out.println("\t found " + lst.count() + " decoded");
    lst = lst.where(q -> q.isActive());
    System.out.println("\t found " + lst.count() + " active");
    lst = lst.where(q -> q.tryGetSingleIcaoCode() != null);
    System.out.println("\t found " + lst.count() + " airlines with exatly one ICAO code");
    IMap<String, IList<AirlineInfo>> map = lst.groupBy(q -> q.tryGetSingleIcaoCode());
    System.out.println("\t found " + map.getKeys().count() + " unique ICAO codes");
    System.out.println("\t duplicate ICAOs:");
    for (Map.Entry<String, IList<AirlineInfo>> entry : map.where(q -> q.getValue().count() > 1)) {
      System.out.println("\t\t" + entry.getKey() + " - " + entry.getValue().count());
      System.out.println("\t\t used will be: " + entry.getValue().get(0).getName());
    }

    ISet<String> allUsedTypes = lst.selectMany(q -> q.getFleet().getKeys().toList()).toSet();
    ISet<String> unknownTypes = allUsedTypes.minus(typeMap.getKeys());
    if (unknownTypes.isEmpty() == false) {
      System.out.println("\t unknown plane types:");
      for (String unknownType : unknownTypes) {
        System.out.println("\t\t" + unknownType);
      }
    }

    System.out.println("\t creating xml fleet file");

    EStringBuilder sb = new EStringBuilder();
    sb.appendLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    sb.appendLine("<fleets>");

    IList<AirlineInfo> finalList = map.getValues()
        .toList()
        .select(q->q.getFirst())
        .orderBy(q->q.tryGetSingleIcaoCode());
    for (AirlineInfo ai : finalList) {
      sb.appendFormatLine("  <company icao=\"%s\" name=\"%s\">", ai.getCodes().getFirst(q -> q.length() == 3), ai.getName());

      for (String k : ai.getFleet().getKeys()) {
        int v = ai.getFleet().get(k);
        k = typeMap.get(k);
        sb.appendFormatLine("      <type name=\"%s\" weight=\"%d\" />", k, v);
      }
      sb.appendLine("  </company>");
    }

    sb.appendLine("</fleets>");

    String f = getFleetXmlFile().toString();

    try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
      bw.write(sb.toString());
      bw.flush();
    } catch (IOException ex) {
      System.out.println("Failed to write fleets file into " + getFleetXmlFile().toString() + ". " + ExceptionUtils.toFullString(ex));
    }

    System.out.println("Phase 3 - done");
  }

  private static String decodeNextPageUrl(String url) {
    final String regex = "(.+-.)(_(\\d+))?.htm";
    Pattern p = Pattern.compile(regex);
    Matcher m = p.matcher(url);
    m.find();
    String urlBase = m.group(1);
    String s = m.group(3);
    int index = 0;
    if (s != null)
      index = Integer.parseInt(s);
    index += 20;

    String ret = urlBase + "_" + index + ".htm";
    return ret;
  }

  private static <T> T deserializeFromDocument(XmlSerializer ser, String fileName, Class<? extends T> type) {
    XDocument doc;
    try {
      doc = XDocument.load(fileName);
    } catch (EXmlException e) {
      throw new ApplicationException(sf("Unable to load file '%s'.", fileName), e);
    }

    T ret = ser.deserialize(doc.getRoot(), type);

    return ret;
  }

  private static String download2(String urlString) {
    StringBuilder ret = new StringBuilder();
    try {
      try {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection(); // Cast shouldn't fail
        HttpURLConnection.setFollowRedirects(true);
        conn.setRequestProperty("User-agent", "Mozilla/5.0 (Windows NT x.y; Win64; x64; rv:10.0) Gecko/20100101 Firefox/10.0");

        InputStream inStr = conn.getInputStream();
        BufferedReader in = new BufferedReader(
            new InputStreamReader(inStr));

        String inputLine;
        while ((inputLine = in.readLine()) != null)
          ret.append(inputLine);
        in.close();

      } catch (Exception ex) {
        throw new ApplicationException(ex);
      }
    } catch (Exception ex) {
      throw new ApplicationException(ex);
    }

    try {
      Thread.sleep(DOWNLOAD_DELAY);
    } catch (InterruptedException e) {
    }

    return ret.toString();
  }

  private static void downloadAirlineFleet(AirlineInfo airline) {
    String cnt = download2(airline.getUrl());

    final String codesRegex = ">Codes<\\/td>.+?>(.*?)<\\/td>";
    Pattern p = Pattern.compile(codesRegex);
    Matcher m = p.matcher(cnt);
    if (m.find()) {
      String codes = m.group(1).trim();
      String[] tmp = codes.split(" ");
      airline.getCodes().addMany(tmp);
    }

    final String typeRegex = "View production.+?<b>(.+?)<\\/b>.+?<b>(.*?)<\\/b>";
    p = Pattern.compile(typeRegex);
    m = p.matcher(cnt);
    while (m.find()) {
      String type = m.group(1).trim();
      String countS = m.group(2);
      if (countS != null) countS = countS.trim();
      if (countS != null && countS.length() > 0) {
        try {
          int count = Integer.parseInt(countS.trim());
          airline.getFleet().set(type, count);
        } catch (Exception ex) {
          System.out.println(sf("Airline %s - type %s - failed to parse value %s to string", airline.getName(), type, countS));
        }
      }
    }

    airline.setDecoded(true);
  }

  private static void downloadAirlines() throws EXmlException {

    System.out.println("Phase 1 - downloading airlines");

    XmlSerializer ser = getSerializer();
    IList<AirlineInfo> lst;
    if (java.nio.file.Files.exists(getAirlinesListFile())) {
      System.out.println("\t file " + getAirlinesListFile().toString() + " already exists, loading");
      lst = (IList<AirlineInfo>) deserializeFromDocument(ser, getAirlinesListFile().toString(), EList.class);
    } else {
      lst = new EList<>();
    }

    char firstChar;
    if (lst.isEmpty()) {
      firstChar = 'a';
    } else {
      firstChar = Character.toLowerCase(lst.getLast().getName().charAt(0));
      firstChar = (char) ((int) firstChar + 1);
      System.out.println("\t starting from character " + firstChar);
    }


    String base = "https://www.airfleets.net/recherche/list-airline-%s.htm";
    for (int i = firstChar; i <= 'z'; i++) {
      char c = (char) i;
      String s = sf(base, c);
      IList<AirlineInfo> tmp = downloadAirlinesByLetter(s);
      lst.addMany(tmp);

      serializeToDocument(ser, lst, getAirlinesListFile().toString());
    }

    lst = lst.where(q -> q.isActive());
    serializeToDocument(ser, lst, getAirlinesListFile().toString());

    System.out.println("Phase 1 - done");
  }

  private static IList<AirlineInfo> downloadAirlinesByLetter(String url) {
    System.out.println("\t" + url);

    IList<AirlineInfo> ret = new EList<>();
    String cnt = download2(url);

    final String regex = "<a class=\"lien\" title=\".+?\"  href=\"(.+?)\">(.+?)<\\/a><br>.+?(\\/(no)?active\\.gif)";
    Pattern p = Pattern.compile(regex);
    Matcher m = p.matcher(cnt);

    while (m.find()) {
      String au = m.group(1);
      String an = m.group(2);
      boolean isActive = m.group(4) == null;

      au = "https://www.airfleets.net/" + au.substring(3);

      AirlineInfo ai = new AirlineInfo(an, au, isActive);
      ret.add(ai);
    }

    String nextPageUrl = decodeNextPageUrl(url);
    String nextPageUrlPart = nextPageUrl.substring(36);
    if (cnt.contains(nextPageUrlPart)) {
      IList<AirlineInfo> tmp = downloadAirlinesByLetter(nextPageUrl);
      ret.addMany(tmp);
    }

    return ret;
  }

  private static void downloadAirlinesFleets() {

    System.out.println("Phase 2 - downloading airlines");

    IList<AirlineInfo> src;
    XmlSerializer ser = getSerializer();

    src = (IList<AirlineInfo>) deserializeFromDocument(ser, getAirlinesListFile().toString(), EList.class);
    int savedCount = 0;
    int saveStep = 10;

    System.out.println("Total airlines: " + src.size());
    int undecodedCount = src.count(q -> q.isDecoded() == false);
    System.out.println("Undecoded airlines: " + undecodedCount);
    IMap<String, IList<AirlineInfo>> uniqueUrls = src.groupBy(q -> q.getUrl());
    src.clear();
    for (Map.Entry<String, IList<AirlineInfo>> entry : uniqueUrls) {
      src.add(entry.getValue().getFirst());
    }
    src.sort(q->q.getName().toLowerCase());
    System.out.println("Unique URLs: " + src.count());
// Prints which URL contains which airlines
//    System.out.println("Groups:");
//    for (Map.Entry<String, IList<AirlineInfo>> entry : uniqueUrls) {
//      System.out.println("\t" + entry.getKey());
//      for (AirlineInfo airlineInfo : entry.getValue()) {
//        System.out.println("\t\t" + airlineInfo.getName() + " (" + airlineInfo.getUrl() + ")");
//      }
//    }

    int index = 0;
    int failedCount = 0;
    for (AirlineInfo airline : src) {
      index++;
      System.out.println("Processing airline " + airline.getName());
      if (airline.isDecoded()) {
        System.out.println("\t already decoded, skipping");
        continue;
      }
      if (airline.isActive() == false) {
        System.out.println("\t not active, skipping");
        airline.setDecoded(true);
        continue;
      }

      try {
        downloadAirlineFleet(airline);
      } catch (Exception ex) {
        System.out.println("\terror!");
        System.out.println(ExceptionUtils.toFullString(ex));
        failedCount++;
        //airline.setDecoded(true);
      }

      savedCount++;
      if (savedCount > saveStep) {
        double proc = (index / (double) src.count()) * 100d;
        System.out.println(sf("\tsaving (processed %d of %d -- %.2f%%, failed %d)", index, src.count(), proc, failedCount));
        serializeToDocument(ser, src, getAirlinesListFile().toString());
        savedCount = 0;
      }
    }

    serializeToDocument(ser, src, getAirlinesListFile().toString());

    System.out.println("Phase 2 - done");
  }

  private static Path getAirlinesListFile() {
    Path ret =
        Paths.get(path.toAbsolutePath().toString(), "airlines.xml");
    return ret;
  }

  private static Path getAirlinesListFinalFile(String postfix) {
    if (postfix == null) postfix = "";
    String name = sf("airlinesFinal%s.xml", postfix);
    Path ret =
        Paths.get(path.toAbsolutePath().toString(), name);
    return ret;
  }

  private static Path getFleetXmlFile() {
    Path ret =
        Paths.get(path.toAbsolutePath().toString(), "fleets.xml");
    return ret;
  }

  private static XmlSerializer getSerializer() {
    XmlSettings sett = new XmlSettings();
    sett.setStoreTypeMode(StoreTypeMode.dontStore);
    if (USE_XML_LOG) sett.getLog().getOnLog().add(new Log.DefaultOnLogHandler());
    sett.getMeta().forClass(EList.class).getRule().with(0, new XmlRule(null, AirlineInfo.class));

    sett.getMeta().forClass(AirlineInfo.class).forField("fleet")
        .getRules().add(
        new XmlRule(null, null)
            .with(0, new XmlRule(null, String.class))
            .with(1, new XmlRule(null, Integer.class)));
    sett.getMeta().forClass(AirlineInfo.class).forField("codes")
        .getRules().add(
        new XmlRule(null, null)
            .with(0, new XmlRule(null, String.class))
    );
    XmlSerializer ret = new XmlSerializer(sett);
    return ret;
  }

  private static <T> void serializeToDocument(XmlSerializer ser, T object, String fileName) {
    XElement elm = new XElement("root");
    ser.serialize(object, elm);
    XDocument doc = new XDocument(elm);
    try {
      doc.save(fileName);
    } catch (EXmlException e) {
      throw new ApplicationException(sf("Unable to save file '%s'.", fileName), e);
    }
  }

  private static void tmp() {
    EDistinctList<String> dst = new EDistinctList<>(q -> q, EDistinctList.Behavior.skip);
    XmlSerializer ser = new XmlSerializer();
    IList<AirlineInfo> lst = (EList<AirlineInfo>) deserializeFromDocument(ser,
        getAirlinesListFile().toString(),
        EList.class);

    lst.forEach(q -> dst.addMany(q.getFleet().getKeys()));

    dst.sort(q -> q);
    for (String s : dst) {
      System.out.println(s);
    }
  }

  static {
    // source: https://www.icao.int/publications/DOC8643/Pages/Search.aspx
    typeMap.set("ATR 42/72", "ATR72");
    typeMap.set("Airbus A300", "A300");
    typeMap.set("Airbus A310", "A310");
    typeMap.set("Airbus A318", "A318");
    typeMap.set("Airbus A319", "A319");
    typeMap.set("Airbus A320", "A320");
    typeMap.set("Airbus A321", "A321");
    typeMap.set("Airbus A330", "A330");
    typeMap.set("Airbus A340", "A340");
    typeMap.set("Airbus A350", "A350");
    typeMap.set("Airbus A380", "A380");
    typeMap.set("BAe 146 / Avro RJ", "B461");
    typeMap.set("Beech 1900D", "B190");
    typeMap.set("Boeing 717", "B717");
    typeMap.set("Boeing 737", "B737");
    typeMap.set("Boeing 737 Next Gen", "B737NG");
    typeMap.set("Boeing 737 NG / Max", "B737NG");
    typeMap.set("Boeing 747", "B747");
    typeMap.set("Boeing 757", "B757");
    typeMap.set("Boeing 767", "B767");
    typeMap.set("Boeing 777", "B777");
    typeMap.set("Boeing 787", "B787");
    typeMap.set("Bombardier C-Series", "BCS1");
    typeMap.set("Airbus A220", "BCS1");
    typeMap.set("Canadair Regional Jet", "CRJ");
    typeMap.set("Comac ARJ21", "AJ21");
    typeMap.set("Dash 8", "DH8A");
    typeMap.set("Embraer 120 Brasilia", "E120");
    typeMap.set("Embraer 135/145", "E145");
    typeMap.set("Embraer 170/175", "E175");
    typeMap.set("Embraer 190/195", "E195");
    typeMap.set("Fokker 50", "F50");
    typeMap.set("Fokker 70/100", "F70");
    typeMap.set("Iliouchine Il-96", "IL96");
    typeMap.set("Lockheed L-1011 TriStar", "L101");
    typeMap.set("McDonnell DC-10", "DC10");
    typeMap.set("McDonnell Douglas DC-10", "DC10");
    typeMap.set("McDonnell MD-11", "MD11");
    typeMap.set("McDonnell Douglas MD-11", "MD11");
    typeMap.set("McDonnell Douglas MD-80/90", "MD90");
    typeMap.set("McDonnell MD-80/90", "MD90");
    typeMap.set("Saab 2000", "SB20");
    typeMap.set("Saab 340", "SF34");
    typeMap.set("Sukhoi SuperJet 100", "SU95");
  }
}
