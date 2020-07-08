package eng.jAtcSim;

import eng.eSystem.eXml.XDocument;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.EXmlException;
import eng.eSystem.utilites.ExceptionUtils;
import eng.eXmlSerialization.XmlException;
import eng.eXmlSerialization.XmlSerializer;
import eng.eXmlSerialization.XmlSettings;
import eng.eXmlSerialization.common.Log;
import eng.eXmlSerialization.meta.TypeInfo;
import eng.eXmlSerialization.meta.XmlRepresentation;
import eng.eXmlSerialization.meta.XmlRule;
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
    {
      TypeInfo ti = xmlSett.getMeta().forClass(StartupSettings.CustomTraffic.class);
      ti.forField("movementsPerHour").getRules().add(
          new XmlRule("item", int.class, XmlRepresentation.element, null)
      );
    }
    xmlSett.getSerializers().add(new LocalTimeAttributeSerializer());
    xmlSett.getLog().getOnLog().add(XmlLoadHelper::log_onLog);
    XmlSerializer ser = new XmlSerializer(xmlSett);

    StartupSettings ret;

    try {
      XDocument doc = XDocument.load(fileName);
      XElement root = doc.getRoot();
      ret = ser.deserialize(root, StartupSettings.class);
    } catch (Exception ex) {
      SharedAcc.getAppLog().writeLine(
          ApplicationLog.eType.warning,
          "Failed to load startup settings from " + fileName +
              ". Defaults used. Reason: " + ExceptionUtils.toFullString(ex, "\n"));
      ret = new StartupSettings();
    }

    return ret;
  }

  private static void log_onLog(Log sender, Log.LogEventArgs e) {
    if (e.type == Log.Type.progressObject) return;
    if (e.type == Log.Type.progressXml) return;
    if (e.type == Log.Type.progressSerializer) return;

    System.out.print("XML ");
    System.out.print(String.format("%-20s", e.type));
    System.out.print(" :: ");
    for (int i = 0; i < e.indent; i++) {
      System.out.print(" ");
    }
    System.out.println(e.message);
  }

  public static void saveStartupSettings(StartupSettings sett, String fileName) {
    XmlSettings xmlSett = new XmlSettings();
    xmlSett.getSerializers().add(new LocalTimeAttributeSerializer());
    xmlSett.getLog().getOnLog().add(XmlLoadHelper::log_onLog);
    XmlSerializer ser = new XmlSerializer(xmlSett);

    try {
      XElement elm = new XElement("startupSettings");
      XDocument doc = new XDocument(elm);
      ser.serialize(sett, elm);

      doc.save(fileName);
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


    RadarStyleSettings ret = null;
    try {
      XDocument doc = XDocument.load(fileName);
      ret = ser.deserialize(doc.getRoot(), RadarStyleSettings.class);
    } catch (EXmlException e) {
      throw new EApplicationException(sf("Unable to load radar-style-settings from '%s'.", fileName), e);
    }
    return ret;
  }

  public static FlightStripSettings loadStripSettings(String fileName) {
    XmlSettings sett = new XmlSettings();
    sett.getSerializers().add(new HexToAwtColorAttributeSerializer());
    sett.getSerializers().add(new AwtFontElementSerializer());

    XmlSerializer ser = new XmlSerializer(sett);

    FlightStripSettings ret = null;
    try {
      XDocument doc = XDocument.load(fileName);
      ret = ser.deserialize(doc.getRoot(), FlightStripSettings.class);
    } catch (EXmlException e) {
      throw new EApplicationException(sf("Unable to load flight-strip-settings from '%s'.", fileName), e);
    }
    return ret;
  }

  public static AppSettings loadApplicationSettings(String fileName) {
    XmlSerializer ser = new XmlSerializer();

    AppSettings ret = null;
    try {
      XDocument doc = XDocument.load(fileName);
      ret = ser.deserialize(doc.getRoot(), AppSettings.class);
    } catch (EXmlException e) {
      throw new EApplicationException(sf("Unable to load application-settings from '%s'.", fileName), e);
    }
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

