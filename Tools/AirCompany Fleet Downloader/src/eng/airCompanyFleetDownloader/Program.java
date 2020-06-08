package eng.airCompanyFleetDownloader;

import eng.eSystem.EStringBuilder;
import eng.eSystem.collections.EDistinctList;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IList;
import eng.eSystem.exceptions.ERuntimeException;
import eng.eSystem.utilites.ExceptionUtils;
import eng.eXmlSerialization.XmlSerializer;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class Program {

  private static final int DOWNLOAD_DELAY = 5000;
  private static final Path path = Paths.get("C:\\temp\\airlines");
  private static EMap<String, String> typeMap = new EMap<>();

  public static void main(String[] params) {
    System.out.println("Target dir " + path.toString());
    try {
      java.nio.file.Files.createDirectory(path);
    } catch (IOException e) {
      System.out.println("Failed to create target dir. Exit");
      return;
    }

    //tmp();
    downloadAirlines();
    downloadAirlinesFleets();
    convertToFleets();
  }

  private static void convertToFleets() {
    XmlSerializer ser = new XmlSerializer();
    IList<AirlineInfo> lst = (EList<AirlineInfo>) ser.deserializeFromDocument(
        getAirlinesListFile().toString(),
        EList.class);

    lst = lst.where(q -> q.getCodes().count(p -> p.length() == 3) == 1);

    EStringBuilder sb = new EStringBuilder();
    sb.appendLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    sb.appendLine("<fleets>");

    for (AirlineInfo ai : lst) {
      sb.appendFormatLine("  <company icao=\"%s\" name=\"%s\">", ai.getCodes().getFirst(q -> q.length() == 3), ai.getName());
      sb.appendLine("    <types>");

      for (String k : ai.getFleet().getKeys()) {
        int v = ai.getFleet().get(k);
        k = typeMap.get(k);
        sb.appendFormatLine("      <type name=\"%s\" weight=\"%d\" />", k, v);
      }

      sb.appendLine("    </types>");
      sb.appendLine("  </company>");
    }

    sb.appendLine("</fleets>");

    String f = getFleetXmlFile().toString();

    try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
      bw.write(sb.toString());
      bw.flush();
    } catch (IOException ex) {

    }

    System.out.println("Done.");
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
        throw new ERuntimeException(ex);
      }
    } catch (Exception ex) {
      throw new ERuntimeException(ex);
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
      airline.getCodes().add(tmp);
    }

    final String typeRegex = "View production.+?<b>(.+?)<\\/b>.+?<b>(.*?)<\\/b>";
    p = Pattern.compile(typeRegex);
    m = p.matcher(cnt);
    while (m.find()) {
      String type = m.group(1);
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

  private static void downloadAirlines() {

    IList<AirlineInfo> lst = new EList<>();
    XmlSerializer ser = new XmlSerializer();

    String base = "http://www.airfleets.net/recherche/list-airline-%s.htm";
    for (int i = 'a'; i <= 'z'; i++) {
      char c = (char) i;
      String s = sf(base, c);
      IList<AirlineInfo> tmp = downloadAirlinesByLetter(s);
      lst.add(tmp);

      ser.serializeToDocument(lst, getTmpFile().toString());
    }

    lst = lst.where(q -> q.isActive());

    ser.serializeToDocument(lst, getAirlinesListFile().toString());
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

      au = "http://www.airfleets.net/" + au.substring(3);

      AirlineInfo ai = new AirlineInfo(an, au, isActive);
      ret.add(ai);
    }

    String nextPageUrl = decodeNextPageUrl(url);
    String nextPageUrlPart = nextPageUrl.substring(35);
    if (cnt.contains(nextPageUrlPart)) {
      IList<AirlineInfo> tmp = downloadAirlinesByLetter(nextPageUrl);
      ret.add(tmp);
    }

    return ret;
  }

  private static void downloadAirlinesFleets() {
    IList<AirlineInfo> src;
    XmlSerializer ser = new XmlSerializer();
    src = (IList<AirlineInfo>) ser.deserializeFromDocument(getAirlinesListFile().toString(), EList.class);
    int savedCount = 0;
    int saveStep = 10;

    System.out.println("Total airlines: " + src.size());
    int undecodedCount = src.count(q -> q.isDecoded() == false);
    System.out.println("Undecoded airlines: " + undecodedCount);

    System.out.println("Checking duplicit");
    IList<AirlineInfo> toRem = new EList<>();
    for (AirlineInfo a : src) {
      for (AirlineInfo b : toRem) {
        if (a.getName().equals(b.getName()) && a != b) {
          toRem.add(b);
        }
      }
    }
    System.out.println("Found " + toRem.size() + " duplicit records, removing...");
    for (AirlineInfo b : toRem) {
      src.remove(b);
    }

    for (AirlineInfo airline : src) {
      if (airline.isDecoded()) continue;
      if (airline.isActive() == false) {
        airline.setDecoded(true);
        continue;
      }

      System.out.println("Processing airline " + airline.getName());

      try {
        downloadAirlineFleet(airline);
      } catch (Exception ex) {
        System.out.println("\terror!");
        System.out.println(ExceptionUtils.toFullString(ex));
        airline.setDecoded(true);
      }

      savedCount++;
      if (savedCount > saveStep) {
        System.out.println("\tsaving");
        ser.serializeToDocument(src, getAirlinesListFile().toString());
        savedCount = 0;
      }
    }

    ser.serializeToDocument(src, getAirlinesListFile().toString());
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

  private static Path getTmpFile() {
    Path ret =
        Paths.get(path.toAbsolutePath().toString(), "tmp.xml");
    return ret;
  }

  private static void tmp() {
    EDistinctList<String> dst = new EDistinctList<>(q -> q, EDistinctList.Behavior.skip);
    XmlSerializer ser = new XmlSerializer();
    IList<AirlineInfo> lst = (EList<AirlineInfo>) ser.deserializeFromDocument(
        getAirlinesListFile().toString(),
        EList.class);

    lst.forEach(q -> dst.add(q.getFleet().getKeys()));

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
    typeMap.set("Boeing 747", "B747");
    typeMap.set("Boeing 757", "B757");
    typeMap.set("Boeing 767", "B767");
    typeMap.set("Boeing 777", "B777");
    typeMap.set("Boeing 787", "B787");
    typeMap.set("Bombardier C-Series", "BCS1");
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
    typeMap.set("McDonnell Douglas DC-10", "DC10");
    typeMap.set("McDonnell Douglas MD-11", "MD11");
    typeMap.set("McDonnell Douglas MD-80/90", "MD90");
    typeMap.set("Saab 2000", "SB20");
    typeMap.set("Saab 340", "SF34");
    typeMap.set("Sukhoi SuperJet 100", "SU95");
  }
}
