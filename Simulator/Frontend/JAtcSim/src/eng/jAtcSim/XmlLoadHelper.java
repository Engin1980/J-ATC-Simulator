package eng.jAtcSim;

import eng.eSystem.utilites.ExceptionUtils;
import eng.eXmlSerialization.XmlException;
import eng.eXmlSerialization.XmlSerializer;
import eng.eXmlSerialization.XmlSettings;
import eng.eXmlSerialization.serializers.AttributeSerializer;
import eng.eXmlSerialization.serializers.implemented.java_awt.AwtFontElementSerializer;
import eng.eXmlSerialization.serializers.implemented.java_awt.HexToAwtColorAttributeSerializer;
import eng.jAtcSim.abstractRadar.parsing.RadarColorAttributeSerializer;
import eng.jAtcSim.abstractRadar.parsing.RadarFontElementSerializer;
import eng.jAtcSim.abstractRadar.settings.RadarStyleSettings;
import eng.jAtcSim.app.startupSettings.StartupSettings;
import eng.jAtcSim.frmPacks.shared.FlightStripSettings;
import eng.jAtcSim.newLib.shared.context.SharedAcc;
import eng.jAtcSim.newLib.shared.logging.ApplicationLog;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class XmlLoadHelper {

  //  public static Object deserialize(String fileName, Class type) {
//    Object ret = deserialize(fileName, type, new XmlSettings());
//    return ret;
//  }
//
//  public static Object deserialize(String fileName, Class type, XmlSettings xmlSerializationSettings) {
//    XmlSerializer ser = new XmlSerializer(xmlSerializationSettings);
//
//    Object ret;
//    try {
//      ret = ser.deserialize(fileName, type);
//    } catch (Exception ex) {
//      throw new ERuntimeException("Failed to deserialize the file '" + fileName + "'.", ex);
//    }
//
//    return ret;
//
//  }
//
  public static StartupSettings loadStartupSettings(String fileName) {
    XmlSettings xmlSett = new XmlSettings();
    xmlSett.getSerializers().add(new LocalTimeAttributeSerializer());
    XmlSerializer ser = new XmlSerializer(xmlSett);

    StartupSettings ret;

    try {
      ret = ser.deserializeFromDocument(fileName, StartupSettings.class);
    } catch (Exception ex) {
      SharedAcc.getAppLog().writeLine(
          ApplicationLog.eType.warning,
          "Failed to load startup settings from " + fileName +
              ". Defaults used. Reason: " + ExceptionUtils.toFullString(ex, "\n"));
      ret = new StartupSettings();
    }

    return ret;
  }

  public static void saveStartupSettings(StartupSettings sett, String fileName) {
    XmlSettings xmlSett = new XmlSettings();
    xmlSett.getSerializers().add(new LocalTimeAttributeSerializer());
    XmlSerializer ser = new XmlSerializer(xmlSett);

    try {
      ser.serializeToDocument(sett, fileName);
    } catch (Exception ex) {
      SharedAcc.getAppLog().writeLine(
          ApplicationLog.eType.warning,
          "Failed to save app settings into " + fileName + ". Reason: " + ex.getMessage());
    }
  }
  public static RadarStyleSettings loadNewDisplaySettings(String fileName) {
    XmlSettings sett = new XmlSettings();
    sett.getSerializers().add(new RadarColorAttributeSerializer());
    sett.getSerializers().add(new RadarFontElementSerializer());
    XmlSerializer ser = new XmlSerializer(sett);

    RadarStyleSettings ret = ser.deserializeFromDocument(fileName, RadarStyleSettings.class);
    return ret;
  }

  public static FlightStripSettings loadStripSettings(String fileName) {
    XmlSettings sett = new XmlSettings();
    sett.getSerializers().add(new HexToAwtColorAttributeSerializer());
    sett.getSerializers().add(new AwtFontElementSerializer());

    XmlSerializer ser = new XmlSerializer(sett);

    FlightStripSettings ret = ser.deserializeFromDocument(fileName, FlightStripSettings.class);
    return ret;
  }

  public static AppSettings loadApplicationSettings(String fileName) {
    XmlSerializer ser = new XmlSerializer();
    AppSettings ret = ser.deserializeFromDocument(fileName, AppSettings.class);
    return ret;
  }

}

class LocalTimeAttributeSerializer extends AttributeSerializer {

  private static final String PATTERN = "H:mm";

  @Override
  public boolean acceptsType(Class<?> aClass) {
    return LocalTime.class.equals(aClass);
  }

  @Override
  protected String formatValue(Object o) {
    LocalTime localTime = (LocalTime) o;
    String ret;
    try {
      ret = localTime.format(DateTimeFormatter.ofPattern(PATTERN));
    } catch (Exception ex) {
      throw new XmlException(sf("Failed to format local time (LocalTime) of value " + localTime), ex);
    }
    return ret;
  }

  @Override
  protected Object parseValue(String s) {
    LocalTime ret;
    try {
      ret = LocalTime.parse(s, DateTimeFormatter.ofPattern(PATTERN));
    } catch (Exception ex) {
      throw new XmlException(sf("Failed to parseOld local time (LocalTime) from value " + s + "."), ex);
    }
    return ret;
  }
}

