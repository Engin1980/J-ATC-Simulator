package eng.jAtcSim;

import eng.eSystem.exceptions.ERuntimeException;
import eng.eSystem.utilites.ExceptionUtil;
import eng.eSystem.xmlSerialization.*;
import eng.jAtcSim.frmPacks.shared.FlightStripSettings;
import eng.jAtcSim.lib.airplanes.AirplaneType;
import eng.jAtcSim.lib.airplanes.AirplaneTypes;
import eng.jAtcSim.lib.atcs.AtcTemplate;
import eng.jAtcSim.lib.coordinates.Coordinate;
import eng.jAtcSim.lib.global.PlaneCategoryDefinitions;
import eng.jAtcSim.lib.traffic.DensityBasedTraffic;
import eng.jAtcSim.lib.traffic.FlightListTraffic;
import eng.jAtcSim.lib.traffic.GenericTraffic;
import eng.jAtcSim.lib.traffic.Traffic;
import eng.jAtcSim.lib.traffic.fleets.CompanyFleet;
import eng.jAtcSim.lib.traffic.fleets.FleetType;
import eng.jAtcSim.lib.traffic.fleets.Fleets;
import eng.jAtcSim.lib.world.*;
import eng.jAtcSim.lib.world.approaches.GnssApproach;
import eng.jAtcSim.lib.world.approaches.IafRoute;
import eng.jAtcSim.lib.world.approaches.IlsApproach;
import eng.jAtcSim.lib.world.approaches.UnpreciseApproach;
import eng.jAtcSim.radarBase.DisplaySettings;
import eng.jAtcSim.radarBase.parsing.RadarColorParser;
import eng.jAtcSim.radarBase.parsing.RadarFontParser;
import eng.jAtcSim.startup.startupSettings.StartupSettings;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class XmlLoadHelper {

  public static Object deserialize(String fileName, Class type) {
    Object ret = deserialize(fileName, type, new Settings());
    return ret;
  }

  public static Object deserialize(String fileName, Class type, Settings xmlSerializationSettings) {
    XmlSerializer ser = new XmlSerializer(xmlSerializationSettings);

    Object ret;
    try {
      ret = ser.deserialize(fileName, type);
    } catch (Exception ex) {
      throw new ERuntimeException("Failed to deserialize the file '" + fileName + "'.", ex);
    }

    return ret;

  }

  public static StartupSettings loadStartupSettings(String fileName) {
    Settings xmlSett = new Settings();
    xmlSett.getValueParsers().add(new LocalTimeParser());
    XmlSerializer ser = new XmlSerializer(xmlSett);

    StartupSettings ret;

    try {
      ret = (StartupSettings) ser.deserialize(fileName, StartupSettings.class);
    } catch (Exception ex) {
      System.out.println("Failed to load startupSettings settings from " + fileName + ". Defaults used. Reason: " + ExceptionUtil.toFullString(ex, "\n"));
      ret = new StartupSettings();
    }

    return ret;
  }

  public static void saveStartupSettings(StartupSettings sett, String fileName) {
    Settings xmlSett = new Settings();
    xmlSett.getValueParsers().add(new LocalTimeParser());
    XmlSerializer ser = new XmlSerializer(xmlSett);

    try {
      ser.serialize(fileName, sett);
    } catch (Exception ex) {
      System.out.println("Failed to save startupSettings settings into " + fileName + ". Reason: " + ex.getMessage());
    }
  }

  public static DisplaySettings loadNewDisplaySettings(String fileName) {
    eng.eSystem.xmlSerialization.Settings sett = new eng.eSystem.xmlSerialization.Settings();

    // own parsers
    sett.getValueParsers().add(new RadarColorParser());
    sett.getElementParsers().add(new RadarFontParser());

    eng.eSystem.xmlSerialization.XmlSerializer ser = new eng.eSystem.xmlSerialization.XmlSerializer(sett);
    DisplaySettings ret = (DisplaySettings) deserialize(fileName, DisplaySettings.class, sett);
    return ret;
  }

  public static Area loadNewArea(String fileName) {
    eng.eSystem.xmlSerialization.Settings sett = new eng.eSystem.xmlSerialization.Settings();

    // ignores
    sett.getIgnoredFieldsRegex().add("^_.+");
    sett.getIgnoredFieldsRegex().add("^parent$");
    sett.getIgnoredFieldsRegex().add("^binded$");

    // list mappings
    sett.getListItemMappings().add(
        new XmlListItemMapping("/airports$", Airport.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/runways$", Runway.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/runways/runway/thresholds$", RunwayThreshold.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/inactiveRunways$", InactiveRunway.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/inactiveRunways/runway/thresholds$", InactiveRunwayThreshold.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/approaches$", "ilsApproach", IlsApproach.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/approaches$", "unpreciseApproach", UnpreciseApproach.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/approaches$", "gnssApproach", GnssApproach.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/ilsApproach/categories$",  IlsApproach.Category.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/iafRoutes$",  IafRoute.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/sharedIafRoutes$",  IafRoute.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/routes$", Route.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/sharedRoutes$", Route.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/atcTemplates$", AtcTemplate.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/holds$", PublishedHold.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/vfrPoints$", VfrPoint.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/navaids$", Navaid.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/borders$", Border.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/points$", "point", BorderExactPoint.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/points$", "arc", BorderArcPoint.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/companies$", DensityBasedTraffic.CodeWeight.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/countries$", DensityBasedTraffic.CodeWeight.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/density$", DensityBasedTraffic.HourBlockMovements.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/directions$", DensityBasedTraffic.DirectionWeight.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/trafficDefinitions$", "genericTraffic", GenericTraffic.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/trafficDefinitions$", "densityTraffic", DensityBasedTraffic.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping( "/trafficDefinitions$", "flightListTraffic", FlightListTraffic.class));

    // own parsers
    sett.getValueParsers().add(new CoordinateValueParser());
    sett.getValueParsers().add(new TrafficCategoryDefinitionParser());

    // instance creators
    sett.getInstanceCreators().add(new AreaCreator());

    Area ret = (Area) deserialize(fileName, Area.class, sett);
    return ret;
  }

  public static AirplaneTypes loadPlaneTypes(String fileName) {
    eng.eSystem.xmlSerialization.Settings sett = new eng.eSystem.xmlSerialization.Settings();

    // ignores
    sett.getIgnoredFieldsRegex().add("^_.+");
    sett.getIgnoredFieldsRegex().add("^parent$");
    sett.getIgnoredFieldsRegex().add("^binded$");

    // mappings
    sett.getListItemMappings().add(
        new XmlListItemMapping("planeTypes$", AirplaneType.class));

    // own parsers
    sett.getValueParsers().add(new CoordinateValueParser());

    AirplaneTypes ret = (AirplaneTypes) deserialize(fileName, AirplaneTypes.class, sett);
    return ret;
  }

  public static Fleets loadFleets(String fileName) {
    eng.eSystem.xmlSerialization.Settings sett = new eng.eSystem.xmlSerialization.Settings();

    // mappings
    sett.getListItemMappings().add(
        new XmlListItemMapping("fleets$", CompanyFleet.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/company/types$", FleetType.class));

    Fleets ret = (Fleets) deserialize(fileName, Fleets.class, sett);
    return ret;
  }

  public static FlightStripSettings loadStripSettings(String fileName) {
    eng.eSystem.xmlSerialization.Settings sett = new eng.eSystem.xmlSerialization.Settings();

    sett.getValueParsers().add(
        new eng.eSystem.xmlSerialization.common.parsers.HexToAwtColorValueParser());
    sett.getElementParsers().add(
        new eng.eSystem.xmlSerialization.common.parsers.AwtFontElementParser());

    FlightStripSettings ret = (FlightStripSettings) deserialize(fileName, FlightStripSettings.class, sett);
    return ret;
  }

  public static AppSettings loadApplicationSettings(String fileName) {
    XmlSerializer ser = new XmlSerializer();
    AppSettings ret = (AppSettings) ser.deserialize(fileName, AppSettings.class);
    return ret;
  }

  public static Traffic loadTraffic(String trafficXmlFile) {
    XmlSerializer ser = new XmlSerializer();
    // todo I dont know how to do this as I do not have support for element->type mapping in XMlSerializing for root element
    // it can be probably done via ICUstomElementParser with extension for .deserialize(Element... )?
    throw new  UnsupportedOperationException("Custom traffic XML files are not supported yet.");
  }
}

class CoordinateValueParser implements IValueParser<Coordinate> {

  @Override
  public Class getType() {
    return Coordinate.class;
  }

  @Override
  public Coordinate parse(String s) {
    return Coordinate.parse(s);
  }

  @Override
  public String format(Coordinate coordinate) {
    throw new UnsupportedOperationException();
  }
}

class TrafficCategoryDefinitionParser implements IValueParser<PlaneCategoryDefinitions>{

  @Override
  public Class getType() {
    return PlaneCategoryDefinitions.class;
  }

  @Override
  public PlaneCategoryDefinitions parse(String s) throws XmlDeserializationException {
    PlaneCategoryDefinitions ret = new PlaneCategoryDefinitions(s);
    return ret;
  }

  @Override
  public String format(PlaneCategoryDefinitions trafficCategoryDefinition) throws XmlSerializationException {
    return trafficCategoryDefinition.toString();
  }
}

class AreaCreator implements eng.eSystem.xmlSerialization.IInstanceCreator<Area> {

  @Override
  public Class getType() {
    return Area.class;
  }

  @Override
  public Area createInstance() {
    Area ret = Area.create();
    return ret;
  }
}

class LocalTimeParser implements eng.eSystem.xmlSerialization.IValueParser<LocalTime>{

  private static final String PATTERN = "H:mm";

  @Override
  public Class getType() {
    return LocalTime.class;
  }

  @Override
  public LocalTime parse(String s) throws XmlDeserializationException {
    LocalTime ret;
    try{
      ret = LocalTime.parse(s, DateTimeFormatter.ofPattern(PATTERN));
    }catch (Exception ex){
      throw new XmlDeserializationException(ex, "Failed to parse local time (LocalTime) from value " + s + ".");
    }
    return ret;
  }

  @Override
  public String format(LocalTime localTime) throws XmlSerializationException {
    String ret;
    try{
      ret = localTime.format( DateTimeFormatter.ofPattern(PATTERN));
    } catch (Exception ex){
      throw new XmlSerializationException(ex, "Failed to format local time (LocalTime) of value " + localTime);
    }
    return ret;
  }
}
