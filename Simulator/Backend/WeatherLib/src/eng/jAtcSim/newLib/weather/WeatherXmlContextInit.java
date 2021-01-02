package eng.jAtcSim.newLib.weather;

import eng.jAtcSim.newLib.shared.xml.XmlContextInit;
import eng.jAtcSim.newLib.weather.presets.PresetWeather;
import eng.newXmlUtils.XmlContext;
import eng.newXmlUtils.implementations.ObjectDeserializer;
import eng.newXmlUtils.implementations.ObjectSerializer;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class WeatherXmlContextInit {

  private static final DateTimeFormatter localTimeFormatter = DateTimeFormatter.ISO_TIME;

  public static void prepareXmlContext(XmlContext ctx) {
    if (XmlContextInit.checkCanBeInitialized(ctx, "weather") == false) return;

    ctx.sdfManager.setSerializer(Weather.class, new ObjectSerializer().withValueClassCheck(Weather.class));
    ctx.sdfManager.setDeserializer(Weather.class, new ObjectDeserializer<>());

    ctx.sdfManager.setFormatter(java.time.LocalTime.class, v -> v.format(localTimeFormatter));
    ctx.sdfManager.setParser(java.time.LocalTime.class, (v, c) -> LocalTime.parse(v, localTimeFormatter));

    ctx.sdfManager.setSerializer(PresetWeather.class, new ObjectSerializer().withValueClassCheck(PresetWeather.class));
    ctx.sdfManager.setDeserializer(PresetWeather.class, new ObjectDeserializer<>());

    ctx.sdfManager.setSerializer(WeatherManager.class, new ObjectSerializer()
            .withCustomFieldFormatter("provider", c -> "-"));
    ctx.sdfManager.setDeserializer(WeatherManager.class, new ObjectDeserializer<WeatherManager>()
            .withCustomFieldDeserialization("provider", (e, c) -> c.values.get(WeatherProvider.class)));
  }
}
