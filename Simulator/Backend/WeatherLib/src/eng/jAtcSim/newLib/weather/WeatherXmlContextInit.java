package eng.jAtcSim.newLib.weather;

import eng.jAtcSim.newLib.shared.xml.XmlContextInit;
import eng.newXmlUtils.XmlContext;
import eng.newXmlUtils.implementations.ObjectDeserializer;
import eng.newXmlUtils.implementations.ObjectSerializer;

public class WeatherXmlContextInit {

  public static void prepareXmlContext(XmlContext ctx) {
    if (XmlContextInit.checkCanBeInitialized(ctx, "weather") == false) return;

    ctx.sdfManager.setSerializer(Weather.class, new ObjectSerializer().withValueClassCheck(Weather.class));
    ctx.sdfManager.setDeserializer(Weather.class, new ObjectDeserializer<>());

    ctx.sdfManager.setSerializer(WeatherManager.class, new ObjectSerializer().withIgnoredFields("provider"));
    ctx.sdfManager.setDeserializer(WeatherManager.class, new ObjectDeserializer<WeatherManager>()
            .withCustomFieldDeserialization("provider", (e, c) -> c.values.get("weatherProvider")));
  }
}
