package eng.jAtcSim;

import eng.eSystem.exceptions.ERuntimeException;
import eng.eSystem.utilites.ExceptionUtils;
import eng.eSystem.xmlSerialization.*;
import eng.eSystem.xmlSerialization.common.parsers.AwtFontElementParser;
import eng.eSystem.xmlSerialization.common.parsers.HexToAwtColorValueParser;
import eng.eSystem.xmlSerialization.exceptions.XmlSerializationException;
import eng.eSystem.xmlSerialization.supports.IValueParser;
import eng.jAtcSim.frmPacks.shared.FlightStripSettings;
import eng.jAtcSim.newLib.Acc;
import eng.jAtcSim.newLib.area.global.logging.ApplicationLog;
import eng.jAtcSim.newLib.world.*;
import eng.jAtcSim.newLib.world.xml.ElementFromValueParser;
import eng.jAtcSim.abstractRadar.RadarStyleSettings;
import eng.jAtcSim.abstractRadar.parsing.RadarColorValueParser;
import eng.jAtcSim.abstractRadar.parsing.RadarFontParser;
import eng.jAtcSim.app.startupSettings.StartupSettings;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class XmlLoadHelper {

  public static Object deserialize(String fileName, Class type) {
    Object ret = deserialize(fileName, type, new XmlSettings());
    return ret;
  }

  public static Object deserialize(String fileName, Class type, XmlSettings xmlSerializationSettings) {
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
    XmlSettings xmlSett = new XmlSettings();
    xmlSett.forType(java.time.LocalTime.class).setCustomParser(new LocalTimeParser());
    XmlSerializer ser = new XmlSerializer(xmlSett);

    StartupSettings ret;

    try {
      ret = ser.deserialize(fileName, StartupSettings.class);
    } catch (Exception ex) {
      Acc.log().writeLine(
          ApplicationLog.eType.warning,
          "Failed to load startup settings from " + fileName +
              ". Defaults used. Reason: " + ExceptionUtils.toFullString(ex, "\n"));
      ret = new StartupSettings();
    }

    return ret;
  }

  public static void saveStartupSettings(StartupSettings sett, String fileName) {
    XmlSettings xmlSett = new XmlSettings();
    xmlSett.forType(java.time.LocalTime.class).setCustomParser(new LocalTimeParser());
    XmlSerializer ser = new XmlSerializer(xmlSett);

    try {
      ser.serialize(fileName, sett);
    } catch (Exception ex) {
      Acc.log().writeLine(
          ApplicationLog.eType.warning,
          "Failed to save app settings into " + fileName + ". Reason: " + ex.getMessage());
    }
  }

  public static RadarStyleSettings loadNewDisplaySettings(String fileName) {
    XmlSettings xmlSett = new XmlSettings();

    // own parsers
    xmlSett.forType(eng.jAtcSim.abstractRadar.global.Color.class).setCustomParser(new RadarColorValueParser());
    xmlSett.forType(eng.jAtcSim.abstractRadar.global.Font.class).setCustomParser(new RadarFontParser());

    RadarStyleSettings ret = (RadarStyleSettings) deserialize(fileName, RadarStyleSettings.class, xmlSett);
    return ret;
  }

  public static FlightStripSettings loadStripSettings(String fileName) {
    XmlSettings sett = new XmlSettings();

    sett.forType(java.awt.Color.class).setCustomParser(new HexToAwtColorValueParser());
    sett.forType(java.awt.Font.class).setCustomParser(new AwtFontElementParser());

    FlightStripSettings ret = (FlightStripSettings) deserialize(fileName, FlightStripSettings.class, sett);
    return ret;
  }

  public static AppSettings loadApplicationSettings(String fileName) {
    XmlSerializer ser = new XmlSerializer();
    AppSettings ret = ser.deserialize(fileName, AppSettings.class);
    return ret;
  }

}

class LocalTimeParser implements IValueParser<LocalTime> {

  private static final String PATTERN = "H:mm";

  @Override
  public LocalTime parse(String s) {
    LocalTime ret;
    try {
      ret = LocalTime.parse(s, DateTimeFormatter.ofPattern(PATTERN));
    } catch (Exception ex) {
      throw new XmlSerializationException(sf("Failed to parseOld local time (LocalTime) from value " + s + "."), ex);
    }
    return ret;
  }

  @Override
  public String format(LocalTime localTime) {
    String ret;
    try {
      ret = localTime.format(DateTimeFormatter.ofPattern(PATTERN));
    } catch (Exception ex) {
      throw new XmlSerializationException(sf("Failed to format local time (LocalTime) of value " + localTime), ex);
    }
    return ret;
  }
}

