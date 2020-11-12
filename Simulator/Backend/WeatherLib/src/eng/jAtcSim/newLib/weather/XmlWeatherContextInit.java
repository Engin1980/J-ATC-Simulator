package eng.jAtcSim.newLib.weather;

import eng.jAtcSim.newLib.shared.logging.ApplicationLog;
import eng.jAtcSim.newLib.weather.contextLocal.Context;
import eng.newXmlUtils.XmlContext;
import eng.newXmlUtils.implementations.ObjectSerializer;

public class XmlWeatherContextInit {

  public static void prepareXmlContext(XmlContext ctx) {
    final String key = "_weather XmlContext init";
    if (ctx.values.containsKey(key)) {
      Context.getApp().getAppLog().write(ApplicationLog.eType.warning, key + " already set.");
      return;
    }
    ctx.values.set(key, null);


    ctx.sdfManager.setSerializer(
            Weather.class,
            new ObjectSerializer().withValueClassCheck(Weather.class, false));
    ctx.sdfManager.setSerializer(WeatherManager.class, new ObjectSerializer()
            .withIgnoredField("provider"));
  }
}
