package eng.jAtcSim.newLib.gameSim.xml;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.gameSim.game.sources.*;
import eng.jAtcSim.newLib.weather.Weather;
import eng.jAtcSimLib.xmlUtils.Deserializer;
import eng.jAtcSimLib.xmlUtils.XmlLoadUtils;
import eng.jAtcSimLib.xmlUtils.deserializers.ObjectDeserializer;
import eng.jAtcSimLib.xmlUtils.serializers.DefaultXmlNames;

public class WeatherSourceDeserializer implements Deserializer {
  @Override
  public Object deserialize(XElement element) {
    WeatherSource ret;
    String className = element.getAttribute(DefaultXmlNames.CLASS_NAME);
    switch (className) {
      case "WeatherXmlSource":
        ret = SourceFactory.createWeatherXmlSource(element.getContent());
        break;
      case "WeatherUserSource":
        ret = SourceFactory.createWeatherUserSource(new Weather());
        XmlLoadUtils.Field.restoreField(element, ret, "initialWeather",
                ObjectDeserializer.createFor(Weather.class));
        break;
      case "WeatherOnlineSource":
        ret = SourceFactory.createWeatherOnlineSource( "", new Weather());
        XmlLoadUtils.Field.restoreField(element, ret, "fallbackWeather", ObjectDeserializer.createFor(Weather.class));
        XmlLoadUtils.Field.restoreField(element, ret, "icao");
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
