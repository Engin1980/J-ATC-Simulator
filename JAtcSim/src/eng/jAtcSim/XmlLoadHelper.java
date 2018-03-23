package eng.jAtcSim;

import eng.eSystem.utilites.ExceptionUtil;
import eng.eSystem.xmlSerialization.*;
import eng.jAtcSim.frmPacks.shared.FlightStripSettings;
import eng.jAtcSim.lib.airplanes.AirplaneType;
import eng.jAtcSim.lib.airplanes.AirplaneTypes;
import eng.jAtcSim.lib.atcs.AtcTemplate;
import eng.jAtcSim.lib.coordinates.Coordinate;
import eng.jAtcSim.lib.exceptions.ERuntimeException;
import eng.jAtcSim.lib.traffic.DensityBasedTraffic;
import eng.jAtcSim.lib.traffic.FlightListTraffic;
import eng.jAtcSim.lib.traffic.GenericTraffic;
import eng.jAtcSim.lib.traffic.Traffic;
import eng.jAtcSim.lib.traffic.fleets.CompanyFleet;
import eng.jAtcSim.lib.traffic.fleets.FleetType;
import eng.jAtcSim.lib.traffic.fleets.Fleets;
import eng.jAtcSim.lib.world.*;
import eng.jAtcSim.lib.world.approaches.ApproachOld;
import eng.jAtcSim.radarBase.DisplaySettings;
import eng.jAtcSim.radarBase.parsing.RadarColorParser;
import eng.jAtcSim.radarBase.parsing.RadarFontParser;
import eng.jAtcSim.startup.StartupSettings;

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
    XmlSerializer ser = new XmlSerializer();

    StartupSettings ret;

    try {
      ret = (StartupSettings) deserialize(fileName, StartupSettings.class);
    } catch (Exception ex) {
      System.out.println("Failed to load startup settings from " + fileName + ". Defaults used. Reason: " + ExceptionUtil.toFullString(ex, "\n"));
      ret = new StartupSettings();
    }

    return ret;
  }

  public static void saveStartupSettings(StartupSettings sett, String fileName) {
    XmlSerializer ser = new XmlSerializer();

    try {
      ser.serialize(fileName, sett);
    } catch (Exception ex) {
      System.out.println("Failed to save startup settings into " + fileName + ". Reason: " + ex.getMessage());
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
        new XmlListItemMapping("/thresholds$", RunwayThreshold.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/approaches$", ApproachOld.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/routes$", Route.class));
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
    throw new  UnsupportedOperationException();
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
