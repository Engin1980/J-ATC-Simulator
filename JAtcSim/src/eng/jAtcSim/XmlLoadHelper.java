package eng.jAtcSim;

import eng.eSystem.exceptions.ERuntimeException;
import eng.eSystem.utilites.ExceptionUtils;
import eng.eSystem.xmlSerialization.*;
import eng.jAtcSim.frmPacks.shared.FlightStripSettings;
import eng.jAtcSim.lib.airplanes.AirplaneType;
import eng.jAtcSim.lib.airplanes.AirplaneTypes;
import eng.jAtcSim.lib.atcs.AtcTemplate;
import eng.jAtcSim.lib.coordinates.Coordinate;
import eng.jAtcSim.lib.global.PlaneCategoryDefinitions;
import eng.jAtcSim.lib.global.sources.AirplaneTypesXmlSource;
import eng.jAtcSim.lib.global.sources.AreaXmlSource;
import eng.jAtcSim.lib.global.sources.FleetsXmlSource;
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
import eng.jAtcSim.radarBase.RadarStyleSettings;
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
      System.out.println("Failed to load startup settings from " + fileName + ". Defaults used. Reason: " + ExceptionUtils.toFullString(ex, "\n"));
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
      System.out.println("Failed to save startup settings into " + fileName + ". Reason: " + ex.getMessage());
    }
  }

  public static RadarStyleSettings loadNewDisplaySettings(String fileName) {
    eng.eSystem.xmlSerialization.Settings sett = new eng.eSystem.xmlSerialization.Settings();

    // own parsers
    sett.getValueParsers().add(new RadarColorParser());
    sett.getElementParsers().add(new RadarFontParser());

    eng.eSystem.xmlSerialization.XmlSerializer ser = new eng.eSystem.xmlSerialization.XmlSerializer(sett);
    RadarStyleSettings ret = (RadarStyleSettings) deserialize(fileName, RadarStyleSettings.class, sett);
    return ret;
  }

  public static Area loadNewArea(String fileName) {
    AreaXmlSource axs = new AreaXmlSource(fileName);
    axs.load();
    Area ret = axs._get();
    return ret;
  }

  public static AirplaneTypes loadPlaneTypes(String fileName) {
    AirplaneTypesXmlSource atxs = new AirplaneTypesXmlSource(fileName);
    atxs.load();
    AirplaneTypes ret = atxs._get();
    return ret;
  }

  public static Fleets loadFleets(String fileName) {
    FleetsXmlSource fxs = new FleetsXmlSource(fileName);
    fxs.load();
    Fleets ret = fxs._get();
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

