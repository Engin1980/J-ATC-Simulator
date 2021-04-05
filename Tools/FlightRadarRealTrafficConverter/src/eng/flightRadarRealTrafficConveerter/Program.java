package eng.flightRadarRealTrafficConveerter;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IMap;
import eng.eSystem.eXml.XDocument;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EXmlException;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.geocoding.HereGeocoding;
import eng.eSystem.geo.geocoding.IGeocoding;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Program {
  private static final String IN_FILE = "in.csv";
  private static final String COMPANIES__FILE = "..\\db\\companies.csv";
  private static final String AIRPORTS_FILE = "..\\db\\airports.csv";
  private static final String OUT_FILE = "out.xml";
  private static final String TYPES_FILE = "..\\db\\typeMaps.csv";
  private static final String HERE_API_FILE = "..\\db\\hereApiKeys.txt";
  private static final NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  private static IMap<String, Coordinate> cities = null;
  private static IMap<String, String> companies = null;
  private static IMap<String, String> types = null;
  private static IGeocoding geocoding = null;
  private static Scanner sc = new Scanner(System.in);

  public static void main(String[] args) throws IOException, EXmlException {
    System.out.println("Download data from FlightRadar24 history - departures and arrivals.");
    System.out.println("Put all the data in the one list in the Excel file.");
    System.out.println("Append to the last column the appropriate 'Departure' or 'Arrival' text");
    System.out.println("Save the file as csv with delimiter ;, name is \"in.csv\".");
    System.out.println("It will have content like:");
    System.out.println("0:05;QS1240;Hurghada (HRG);SmartWings ;B738 (OK-TSR) ;Departed 00:18;Departure");
    System.out.println("....");
    System.out.println("Load the file using this app. The file path is the first parameter. This tool will create flights output to be put in the xml traffic file.");

    if (args.length == 0)
      System.out.println("No input path, exiting.");

    String path = args[0];
    Path inPath = Paths.get(path, IN_FILE);
    Path companiesPath = Paths.get(path, COMPANIES__FILE);
    Path airportsPath = Paths.get(path, AIRPORTS_FILE);
    Path apiPath = Paths.get(path, HERE_API_FILE);
    Path typesPath = Paths.get(path, TYPES_FILE);
    Path outPath = Paths.get(path, OUT_FILE);

    System.out.println("Loading airport cities coordinates database...");
    cities = CsvLoader.loadAirports(airportsPath);
    System.out.println("... loaded " + cities.size() + " records.");

    System.out.println("Loading company IATA->ICAO database...");
    companies = CsvLoader.loadTouples(companiesPath);
    System.out.println("... loaded " + companies.size() + " records.");

    System.out.println("Loading company airplane-type-map database...");
    types = CsvLoader.loadTouples(typesPath);
    System.out.println("... loaded " + types.size() + " records.");

    System.out.println("Loading here-maps api keys...");
    String[] keys = CsvLoader.loadHereApiKeys(apiPath);
    if (keys == null)
      System.out.println("... here-maps keys were not found. User GPS enter will be used.");
    else {
      geocoding = new HereGeocoding(keys[0], keys[1]);
      System.out.println("... here-maps keys found and used.");
    }

    System.out.println("Loading input file...");
    IList<String> lines = EList.of(java.nio.file.Files.readAllLines(inPath));
    System.out.println("... loaded " + lines.size() + " records.");

    IList<Item> items = new EList();
    System.out.println("Begin conversion...");
    convertFromStringToItems(lines, items);

    System.out.println("Replacing airplane types...");
    convertTypes(items);

    System.out.println("Convert IATA->ICAO...");
    int cnt = companies.size();
    convertIataToIcaos(items);
    if (cnt == companies.size()) {
      System.out.println("... saving updated database");
      CsvLoader.saveCompanies(companies, companiesPath);
    }

    System.out.println("Guess arrival/departure radials...");
    cnt = cities.size();
    geussRadials(items);
    if (cnt != cities.size()) {
      System.out.println("... saving updated database");
      CsvLoader.saveAirports(cities, airportsPath);
    }

    System.out.println("Binding...");
    bindItems(items);

    XElement root = generateXml(items);

    XDocument doc = new XDocument(root);
    doc.save(outPath);
    System.out.println("Done, saved as '" + outPath + "'.");
  }

  private static void convertTypes(IList<Item> items) {
    for (Item item : items) {
      if (types.containsKey(item.type))
        item.type = types.get(item.type);
    }
  }

  private static void convertFromStringToItems(IList<String> lines, IList<Item> items) {
    for (String line : lines) {
      System.out.println("... converting " + line);
      Item item = convertLine(line);
      items.add(item);
    }
  }

  private static XElement generateXml(IList<Item> items) {
    System.out.println("Generating result xml-file...");
    XElement trafficDefinition = new XElement("trafficDefinition");
    trafficDefinition.setAttribute("xmlns", "https://github.com/Engin1980/J-ATC-Simulator/traffic");

    XElement meta = new XElement("meta");
    meta.addElement(new XElement("title", "Automatically generated traffic."));
    meta.addElement(new XElement("author", "Flight radar real traffic converter"));
    meta.addElement(new XElement("description", "Automatically generated flight-list-traffic generated on " +
            java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("d. MMMM yyyy, HH:mm:ss"))
            + "."));
    meta.addElement(new XElement("date",
            java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    ));
    trafficDefinition.addElement(meta);

    XElement flightListTraffic = new XElement("flightListTraffic");
    flightListTraffic.setAttribute("delayProbability", "0");
    flightListTraffic.setAttribute("maxDelayInMinutesPerStep", "0");
    trafficDefinition.addElement(flightListTraffic);

    XElement flights = new XElement("flights");
    flightListTraffic.addElement(flights);

    XElement tmp;
    for (Item item : items) {
      tmp = createElement(item);
      flights.addElement(tmp);
    }
    return trafficDefinition;
  }

  private static void geussRadials(IList<Item> items) {
    for (Item item : items) {
      Coordinate c = getCoordinateOf(item.otherAirport);
      item.otherAirportCoordinate = c;
    }
  }

  private static Coordinate getCoordinateOf(String name) {
    Coordinate ret = null;
    if (cities.containsKey(name) == false) {
      if (geocoding != null) {
        try {
          System.out.println("... downloading coordinate of " + name);
          ret = geocoding.geocode(name);
        } catch (Exception ex) {
          System.out.println("... geocoding ERROR for " + name + ": " + ex.getMessage());
          ret = null;
        }
      }
      if (ret == null) {
        System.out.println("Enter coordinates for " + name + ":");
        String line = sc.nextLine();
        ret = Coordinate.parse(line);
      }
      cities.set(name, ret);
    } else
      ret = cities.get(name);
    return ret;
  }

  private static void convertIataToIcaos(IList<Item> items) {
    for (Item item : items) {
      String iata = item.callsignCompany;
      String icao;
      if (companies.containsKey(iata) == false) {
        System.out.println("** Unknown IATA code '" + iata + "'. Enter corresponding ICAO code:");
        icao = sc.nextLine();
        companies.set(iata, icao);
      } else
        icao = companies.get(iata);
      item.callsignCompany = icao;
    }
  }

  private static XElement createElement(Item item) {
    XElement ret = new XElement("flight");

    ret.setAttribute("time",
            item.time.format(DateTimeFormatter.ofPattern("H:mm")));
    ret.setAttribute("callsign",
            item.callsignCompany + item.callsignNumber);
    ret.setAttribute("kind",
            item.arrival ? "arrival" : "departure");
    String coordString = nf.format(item.otherAirportCoordinate.getLatitude().get()) + " " +
            nf.format(item.otherAirportCoordinate.getLongitude().get());
    ret.setAttribute("otherAirport", coordString);
    ret.setAttribute("planeType", item.type);
    if (item.previousItem != null)
      ret.setAttribute("follows",
              item.callsignCompany + item.callsignNumber);

    return ret;
  }

  private static void bindItems(IList<Item> items) {
    items.sort(q -> q.time);

    IMap<String, Item> tmp = new EMap<>();
    for (Item item : items) {
      String reg = item.registration;
      if (item.arrival) {
        tmp.set(reg, item);
      } else {
        item.previousItem = tmp.tryGet(reg).orElse(item.previousItem);
      }
    }
  }

  private static Item convertLine(String line) {
    line = line.replace(" ;", ";");
    final String regex = "((\\d{1,2}):(\\d{2}));([0-9A-Z]{3,6});(.+?) \\(.+\\);(.+?);(.+?)(?: (\\((.+)\\)))?;.+?;(Departure|Arrival)";
    Pattern pattern = Pattern.compile(regex);
    Matcher m = pattern.matcher(line);
    if (m.find() == false)
      throw new RuntimeException("Failed to decode line: " + line + ".");

    Item ret = new Item();
    ret.time = java.time.LocalTime.of(
            Integer.parseInt(m.group(2)),
            Integer.parseInt(m.group(3)));
    String clsgn = m.group(4);
    ret.callsignCompany = clsgn.substring(0, 2);
    ret.callsignNumber = clsgn.substring(2);
    ret.otherAirport = m.group(5);
    ret.type = m.group(7);
    ret.registration = m.group(9);
    ret.arrival = m.group(10).equals("Arrival");

    return ret;
  }
}

class Item {
  public java.time.LocalTime time;
  public String callsignCompany;
  public String callsignNumber;
  public String otherAirport;
  public Coordinate otherAirportCoordinate;
  public String type;
  public boolean arrival;
  public String registration;
  public Item previousItem;
}
