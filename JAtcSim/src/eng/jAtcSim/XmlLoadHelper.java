package eng.jAtcSim;

import eng.eSystem.xmlSerialization.*;
import eng.jAtcSim.frmPacks.shared.FlightStripSettings;
import eng.jAtcSim.lib.traffic.DensityBasedTraffic;
import eng.jAtcSim.lib.traffic.FlightListTraffic;
import eng.jAtcSim.lib.traffic.GenericTraffic;
import eng.jAtcSim.lib.traffic.Traffic;
import eng.jAtcSim.lib.world.*;
import eng.jAtcSim.radarBase.DisplaySettings;
import eng.jAtcSim.radarBase.parsing.RadarColorParser;
import eng.jAtcSim.radarBase.parsing.RadarFontParser;
import eng.jAtcSim.startup.StartupSettings;
import eng.jAtcSim.lib.airplanes.AirplaneType;
import eng.jAtcSim.lib.airplanes.AirplaneTypes;
import eng.jAtcSim.lib.atcs.AtcTemplate;
import eng.jAtcSim.lib.coordinates.Coordinate;
import eng.jAtcSim.lib.exceptions.ERuntimeException;

public class XmlLoadHelper {

  public static Object deserialize(String fileName, Class type) {
    Object ret = deserialize(fileName, type, new Settings());
    return ret;
  }

  public static Object deserialize(String fileName, Class type, Settings xmlSerializationSettings) {
    XmlSerializer ser = new XmlSerializer(xmlSerializationSettings);

    Object ret;
    try{
      ret = ser.deserialize(fileName, type);
    } catch (Exception ex){
      StringBuilder sb = new StringBuilder();
      Throwable t = ex;
      while (t != null) {
        sb.append(t.getMessage());
        sb.append(" # # # # # ");
        t = t.getCause();
      }
      throw new ERuntimeException(sb.toString(), ex);
    }

    return ret;

  }

  public static StartupSettings loadStartupSettings(String fileName) {
    XmlSerializer ser = new XmlSerializer();

    StartupSettings ret;

    try {
      ret = (StartupSettings) deserialize(fileName, StartupSettings.class);
    } catch (Exception ex) {
      System.out.println("Failed to load startup settings from " + fileName + ". Defaults used. Reason: " + ex.getMessage());
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

    // mappings
    sett.getListItemMapping().add(
        new XmlListItemMapping("airports", Airport.class));
    sett.getListItemMapping().add(
        new XmlListItemMapping("runways", Runway.class));
    sett.getListItemMapping().add(
        new XmlListItemMapping("thresholds", RunwayThreshold.class));
    sett.getListItemMapping().add(
        new XmlListItemMapping("approaches", Approach.class));
    sett.getListItemMapping().add(
        new XmlListItemMapping("routes", Route.class));
    sett.getListItemMapping().add(
        new XmlListItemMapping("atcTemplates", AtcTemplate.class));
    sett.getListItemMapping().add(
        new XmlListItemMapping("holds", PublishedHold.class));
    sett.getListItemMapping().add(
        new XmlListItemMapping("vfrPoints", VfrPoint.class));
    sett.getListItemMapping().add(
        new XmlListItemMapping("navaids", Navaid.class));
    sett.getListItemMapping().add(
        new XmlListItemMapping("borders", Border.class));
    sett.getListItemMapping().add(
        new XmlListItemMapping("points", "point", BorderExactPoint.class));
    sett.getListItemMapping().add(
        new XmlListItemMapping("points", "arc", BorderArcPoint.class));

    // own parsers
    sett.getValueParsers().add(new CoordinateValueParser());

    // instance creators
    sett.getInstanceCreators().add(new AreaCreator());

    // traffic inherited parsers
    sett.getCustomMappings().add(
        new XmlCustomFieldMapping(Airport.class, "traffic", "genericTraffic", GenericTraffic.class));
    sett.getCustomMappings().add(
        new XmlCustomFieldMapping(Airport.class, "traffic", "densityTraffic", DensityBasedTraffic.class));
    sett.getCustomMappings().add(
        new XmlCustomFieldMapping(Airport.class, "traffic", "flightListTraffic", FlightListTraffic.class));

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
    sett.getListItemMapping().add(
        new XmlListItemMapping("planeTypes", AirplaneType.class));

    // own parsers
    sett.getValueParsers().add(new CoordinateValueParser());

    AirplaneTypes ret = (AirplaneTypes) deserialize(fileName, AirplaneTypes.class,sett);
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
}

class CoordinateValueParser implements IValueParser<Coordinate> {

  @Override
  public String getTypeName() {
    return Coordinate.class.getName();
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

class AreaCreator implements eng.eSystem.xmlSerialization.IInstanceCreator<Area>{

  @Override
  public String getTypeName() {
    return Area.class.getName();
  }

  @Override
  public Area createInstance() {
    Area ret = Area.create();
    return ret;
  }
}
