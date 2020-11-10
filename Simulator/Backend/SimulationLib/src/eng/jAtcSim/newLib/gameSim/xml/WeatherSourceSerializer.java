package eng.jAtcSim.newLib.gameSim.xml;

import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.jAtcSim.newLib.gameSim.game.sources.WeatherOnlineSource;
import eng.jAtcSim.newLib.gameSim.game.sources.WeatherSource;
import eng.jAtcSim.newLib.gameSim.game.sources.WeatherUserSource;
import eng.jAtcSim.newLib.gameSim.game.sources.WeatherXmlSource;
import eng.jAtcSim.newLib.weather.Weather;
import eng.jAtcSimLib.xmlUtils.Serializer;
import eng.jAtcSimLib.xmlUtils.XmlSaveUtils;
import eng.jAtcSimLib.xmlUtils.serializers.DefaultXmlNames;
import eng.jAtcSimLib.xmlUtils.serializers.ObjectSerializer;
//TODEL unused in future, I guess
public class WeatherSourceSerializer implements Serializer<WeatherSource> {
  @Override
  public void invoke(XElement target, WeatherSource value) {
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
      target.setAttribute(DefaultXmlNames.CLASS_NAME, "WeatherUserSource");
      WeatherOnlineSource w = (WeatherOnlineSource) value;
      XmlSaveUtils.Field.storeField(target, w, "icao");
      XmlSaveUtils.Field.storeField(target, w, "fallbackWeather",
              ObjectSerializer.createFor(Weather.class));
    } else {
      throw new EApplicationException("Unsupported weather source type " + value.getClass().getName());
    }
  }
}
