package eng.jAtcSim.newLib.gameSim.xml;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.gameSim.game.sources.WeatherOnlineSource;
import eng.jAtcSim.newLib.gameSim.game.sources.WeatherSource;
import eng.jAtcSim.newLib.gameSim.game.sources.WeatherUserSource;
import eng.jAtcSim.newLib.gameSim.game.sources.WeatherXmlSource;
import eng.jAtcSim.newLib.weather.Weather;
import eng.jAtcSimLib.xmlUtils.Deserializer;
import eng.jAtcSimLib.xmlUtils.XmlLoadUtils;
import eng.jAtcSimLib.xmlUtils.deserializers.ObjectDeserializer;
import eng.jAtcSimLib.xmlUtils.serializers.DefaultXmlNames;

public class WeatherSourceDeserializer implements Deserializer {
  @Override
  public Object deserialize(XElement element, Class<?> type) {
    WeatherSource ret;
    String className = element.getAttribute(DefaultXmlNames.CLASS_NAME);
    switch (className) {
      case "WeatherXmlSource":
        ret = new WeatherXmlSource(element.getContent());
        break;
      case "WeatherUserSource":
        WeatherUserSource weatherUserSource = new WeatherUserSource(new Weather());
        XmlLoadUtils.Field.restoreField(element, weatherUserSource, "initialWeather",
                new ObjectDeserializer());
        ret = weatherUserSource;
        break;
      case "WeatherOnlineSource":
        WeatherOnlineSource weatherOnlineSource = new WeatherOnlineSource(false, "", new Weather());
        XmlLoadUtils.Field.restoreField(element, weatherOnlineSource, "fallbackWeather", new ObjectDeserializer());
        XmlLoadUtils.Field.restoreField(element, weatherOnlineSource, "icao");
        ret = weatherOnlineSource;
        break;
      default:
        throw new UnsupportedOperationException("Unknown weather-source type.");
    }

    return ret;
  }

}

/*
if (value instanceof WeatherXmlSource) {
      target.setAttribute(DefaultXmlNames.CLASS_NAME, "WeatherXmlSource");
      WeatherXmlSource w = (WeatherXmlSource) value;
      XmlSaveUtils.Field.storeField(target, w, "fileName");
    } else if (value instanceof WeatherUserSource) {
      target.setAttribute(DefaultXmlNames.CLASS_NAME, "WeatherUserSource");
      WeatherUserSource w = (WeatherUserSource) value;
      XmlSaveUtils.Field.storeField(target, w, "initialWeather",
              ObjectSerializer.createFor(Weather.class));
    } else if (value instanceof WeatherOnlineSource) {
      target.setAttribute(DefaultXmlNames.CLASS_NAME, "WeatherOnlineSource");
      WeatherOnlineSource w = (WeatherOnlineSource) value;
      XmlSaveUtils.Field.storeField(target, w, "icao");
      XmlSaveUtils.Field.storeField(target, w, "fallbackWeather",
              ObjectSerializer.createFor(Weather.class));
    } else {
      throw new EApplicationException("Unsupported weather source type " + value.getClass().getName());
    }
 */
