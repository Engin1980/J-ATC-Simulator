package eng.jAtcSim.xmlLoading;

import eng.eSystem.collections.IMap;
import eng.eSystem.utilites.StringUtils;
import eng.eXmlSerialization.XmlSerializer;
import eng.eXmlSerialization.XmlSettings;
import eng.eXmlSerialization.common.Log;
import eng.eXmlSerialization.meta.XmlRule;
import eng.eXmlSerialization.serializers.ElementSerializerWrappingAttributeSerializer;
import eng.eXmlSerialization.serializers.implemented.java_awt.AwtFontElementSerializer;
import eng.eXmlSerialization.serializers.implemented.java_awt.HexToAwtColorAttributeSerializer;
import eng.eXmlSerialization.serializers.implemented.java_time.LocalTimeAttributeSerializer;
import eng.jAtcSim.abstractRadar.global.Color;
import eng.jAtcSim.abstractRadar.global.Font;
import eng.jAtcSim.abstractRadar.settings.xml.RadarColorAttributeSerializer;
import eng.jAtcSim.abstractRadar.settings.xml.RadarFontElementSerializer;
import eng.jAtcSim.app.startupSettings.StartupSettings;
import eng.jAtcSim.contextLocal.Context;
import eng.jAtcSim.newLib.shared.logging.ApplicationLog;
import eng.jAtcSim.xmlLoading.serializers.SpeechResponsesDeserializer;

import java.time.LocalTime;

public class XmlSerializationFactory {

  private static final boolean VERBOSE_ERROR = true;
  private static final boolean VERBOSE_PROGRESS_OBJECT = false;
  private static final boolean VERBOSE_PROGRESS_SERIALIZER = false;
  private static final boolean VERBOSE_PROGRESS_XML = false;
  private static final boolean VERBOSE_WARNING = true;

  public static XmlSerializer createForFlightStripSettings() {
    XmlSettings sett = createBasicXmlSettings();
    sett.getSerializers().addAsFirstForType(java.awt.Color.class, new HexToAwtColorAttributeSerializer());
    sett.getSerializers().addAsFirstForType(java.awt.Color.class, new ElementSerializerWrappingAttributeSerializer(new HexToAwtColorAttributeSerializer()));
    sett.getSerializers().addAsFirstForType(java.awt.Font.class, new AwtFontElementSerializer());
    XmlSerializer ret = new XmlSerializer(sett);
    return ret;
  }

  public static XmlSerializer createForRadarStyleSettings() {
    XmlSettings sett = createBasicXmlSettings();
    //sett.getSerializers().addAsFirstForType(java.awt.Color.class, new HexToAwtColorAttributeSerializer());
    //sett.getSerializers().addAsFirstForType(java.awt.Color.class, new ElementSerializerWrappingAttributeSerializer(new HexToAwtColorAttributeSerializer()));
    //sett.getSerializers().addAsFirstForType(java.awt.Font.class, new AwtFontElementSerializer());
    sett.getSerializers().addAsFirstForType(Font.class, new RadarFontElementSerializer());
    sett.getSerializers().addAsFirstForType(
            Color.class,
            new ElementSerializerWrappingAttributeSerializer(new RadarColorAttributeSerializer()));
    sett.getSerializers().addAsFirstForType(
            Color.class,
            new RadarColorAttributeSerializer());
    XmlSerializer ser = new XmlSerializer(sett);
    return ser;
  }

  public static XmlSerializer createForSpeechResponses() {
    XmlSettings sett = createBasicXmlSettings();

    sett.getMeta().forClass(IMap.class).setRule(
            new XmlRule(null, null).with(new SpeechResponsesDeserializer())
    );

    XmlSerializer ser = new XmlSerializer(sett);
    return ser;
  }

  public static XmlSerializer createForStartupSettings() {
    XmlSettings sett = createBasicXmlSettings();
    sett.getSerializers().addAsFirstForType(LocalTime.class, new LocalTimeAttributeSerializer("HH:mm"));
    sett.getMeta().forClass(StartupSettings.CustomTraffic.class)
            .forField("companies")
            .getRules()
            .add(new XmlRule()
                    .with(0, new XmlRule(null, String.class))
                    .with(1, new XmlRule(null, Integer.class)));
    sett.getMeta().forClass(StartupSettings.CustomTraffic.class)
            .forField("countryCodes")
            .getRules()
            .add(new XmlRule()
                    .with(0, new XmlRule(null, String.class))
                    .with(1, new XmlRule(null, Integer.class)));
    XmlSerializer ser = new XmlSerializer(sett);

    return ser;
  }

  private static XmlSettings createBasicXmlSettings() {
    XmlSettings sett = new XmlSettings();

    // set logging
    sett.getLog().getOnLog().add(XmlSerializationFactory::log_onLog);

    return sett;
  }

  private static void log_onLog(Log sender, Log.LogEventArgs e) {
    if (e.type == Log.Type.error && !VERBOSE_ERROR) return;
    if (e.type == Log.Type.warning && !VERBOSE_WARNING) return;
    if (e.type == Log.Type.progressXml && !VERBOSE_PROGRESS_XML) return;
    if (e.type == Log.Type.progressObject && !VERBOSE_PROGRESS_OBJECT) return;
    if (e.type == Log.Type.progressSerializer && !VERBOSE_PROGRESS_SERIALIZER) return;

    ApplicationLog.eType logType;
    if (e.type == Log.Type.error)
      logType = ApplicationLog.eType.critical;
    else if (e.type == Log.Type.warning)
      logType = ApplicationLog.eType.warning;
    else
      logType = ApplicationLog.eType.info;

    String sb = "XML " +
            String.format("%-20s", e.type) +
            " :: " +
            StringUtils.repeat(" ", e.indent) +
            e.message;
    Context.getApp().getAppLog().write(logType, sb);
  }
}
