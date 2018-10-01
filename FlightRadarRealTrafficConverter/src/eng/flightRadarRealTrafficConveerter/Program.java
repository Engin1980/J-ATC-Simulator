package eng.flightRadarRealTrafficConveerter;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IMap;
import eng.eSystem.eXml.XDocument;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EXmlException;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.geo.geocoding.HereGeocoding;
import eng.eSystem.geo.geocoding.IGeocoding;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Program {
  public static final String IN_FILE = "in.csv";
  public static final String COMPANIES__FILE = "..\\db\\companies.csv";
  public static final String AIRPORTS_FILE = "..\\db\\airports.csv";
  private static final String OUT_FILE = "out.xml";
  private static final String HERE_API_FILE = "..\\db\\hereApiKeys.txt";

  private static IMap<String, Coordinate> cities = null;
  private static IMap<String, String> companies = null;
  private static IGeocoding geocoding = null;

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
    Path outPath = Paths.get(path, OUT_FILE);

    System.out.println("Loading airport cities coordinates database...");
    cities = CsvLoader.loadAirports(airportsPath);
    System.out.println("... loaded " + cities.size() + " records.");

    System.out.println("Loading company IATA->ICAO database...");
    companies = CsvLoader.loadCompanies(companiesPath);
    System.out.println("... loaded " + companies.size() + " records.");

    System.out.println("Loading here-maps api keys...");
    String[] keys = CsvLoader.loadHereApiKeys(apiPath);
    if (keys == null)
      System.out.println("... here-maps keys were not found. User GPS enter will be used.");
    else {
      geocoding = new HereGeocoding(keys[0], keys[1]);
      System.out.println("... here-maps keys found and used.");
    }

    System.out.println("Loading input file...");
    IList<String> lines = new EList<>(
        java.nio.file.Files.readAllLines(inPath)
    );
    System.out.println("... loaded " + lines.size() + " records.");

    IList<Item> items = new EList();
    System.out.println("Begin conversion...");
    for (String line : lines) {
      System.out.println("... converting " + line);
      Item item = convertLine(line);
      items.add(item);
    }

    System.out.println("Convert IATA->ICAO...");
    convertIataToIcaos(items);
//    System.out.println("... saving updated database");
//    CsvLoader.saveCompanies(companies, companiesPath);

    System.out.println("Guess arrival/departure radials...");
    geussRadials(items);
    System.out.println("... saving updated database");
    CsvLoader.saveAirports(cities, airportsPath);

    System.out.println("Binding...");
    bindItems(items);

    System.out.println("Generating result xml-file...");
    XElement root = new XElement("root");
    XElement tmp;
    for (Item item : items) {
      tmp = createElement(item);
      root.addElement(tmp);
    }

    XDocument doc = new XDocument(root);
    doc.save(outPath);
    System.out.println("Done, saved as '" + outPath + "'.");
  }

  private static Scanner sc = new Scanner(System.in);

  private static void geussRadials(IList<Item> items) {
    System.out.println("Enter main airport city name:");
    String name = sc.nextLine();
    Coordinate main = getCoordinateOf(name);
    for (Item item : items) {
      Coordinate c = getCoordinateOf(item.otherAirport);
      double radial = Coordinates.getBearing(main, c);
      item.radial = (int) radial;
    }
  }

  private static Coordinate getCoordinateOf(String name) {
    Coordinate ret = null;
    if (cities.containsKey(name) == false){
      if (geocoding != null){
        try {
          System.out.println("... downloading coordinate of " + name);
          ret = geocoding.geocode(name);
        }
        catch (Exception ex){
          System.out.println("... geocoding ERROR for " + name + ": " + ex.getMessage());
          ret = null;
        }
      }
      if (ret == null){
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
    ret.setAttribute("heading", "0");
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
        Item other = tmp.tryGet(reg);
        if (other != null) item.previousItem = other;
      }
    }
  }

  private static Item convertLine(String line) {
    line = line.replace(" ;", ";");
    final String regex = "((\\d{1,2}):(\\d{2}));([0-9A-Z]{4,6});(.+?) \\(.+\\);(.+?);(.+?)(?: (\\((.+)\\)))?;.+?;(Departure|Arrival)";
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
  public int radial;
  public String type;
  public boolean arrival;
  public String registration;
  public Item previousItem;
}