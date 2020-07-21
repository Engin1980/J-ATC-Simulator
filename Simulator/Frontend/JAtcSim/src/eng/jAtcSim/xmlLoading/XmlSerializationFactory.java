package eng.jAtcSim.xmlLoading;

import eng.eSystem.utilites.StringUtils;
import eng.eXmlSerialization.XmlSerializer;
import eng.eXmlSerialization.XmlSettings;
import eng.eXmlSerialization.common.Log;
import eng.eXmlSerialization.serializers.implemented.java_time.LocalTimeAttributeSerializer;
import eng.jAtcSim.contextLocal.Context;
import eng.jAtcSim.newLib.shared.logging.ApplicationLog;

import java.time.LocalTime;

public class XmlSerializationFactory {

  private static final boolean VERBOSE_ERROR = true;
  private static final boolean VERBOSE_PROGRESS_OBJECT = true;
  private static final boolean VERBOSE_PROGRESS_SERIALIZER = true;
  private static final boolean VERBOSE_PROGRESS_XML = true;
  private static final boolean VERBOSE_WARNING = true;

  public static XmlSerializer createForStartupSettings() {
    XmlSettings sett = createBasicXmlSettings();
    sett.getSerializers().addAsFirstForType(LocalTime.class, new LocalTimeAttributeSerializer("HH:mm"));
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
