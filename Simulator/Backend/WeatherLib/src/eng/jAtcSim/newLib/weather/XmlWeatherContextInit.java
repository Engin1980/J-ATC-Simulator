package eng.jAtcSim.newLib.weather;

import eng.jAtcSim.newLib.shared.xml.XmlContextInit;
import eng.newXmlUtils.XmlContext;
import eng.newXmlUtils.implementations.ObjectSerializer;

public class XmlWeatherContextInit {

  public static void prepareXmlContext(XmlContext ctx) {
    if (XmlContextInit.checkCanBeInitialized(ctx, "weather") == false) return;

    ctx.sdfManager.setSerializer(
            Weather.class,
            new ObjectSerializer().withValueClassCheck(Weather.class, false));
    ctx.sdfManager.setSerializer(WeatherManager.class, new ObjectSerializer()
            .withIgnoredField("provider"));
  }
}
