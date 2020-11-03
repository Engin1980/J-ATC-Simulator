package eng.jAtcSim.newLib.weather;

import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.ToDoException;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.weather.decoders.MetarDecoder;
import eng.jAtcSimLib.xmlUtils.XmlLoadUtils;
import eng.jAtcSimLib.xmlUtils.XmlSaveUtils;
import eng.jAtcSimLib.xmlUtils.deserializers.ObjectDeserializer;
import eng.jAtcSimLib.xmlUtils.serializers.ObjectSerializer;

import java.util.TooManyListenersException;

public class WeatherManager {
  private Weather currentWeather;
  private boolean newWeatherFlag;
  private final WeatherProvider provider;

  public WeatherManager(WeatherProvider provider) {
    EAssert.Argument.isNotNull(provider);

    this.provider = provider;
    this.newWeatherFlag = true;
  }

  public static WeatherManager load(WeatherProvider provider, XElement element){
    WeatherManager ret = new WeatherManager(provider);

    XmlLoadUtils.Field.restoreField(element, ret, "newWeatherFlat");
    XmlLoadUtils.Field.restoreField(element, ret, "weather",
            ObjectDeserializer.createFor(Weather.class));

    return ret;
  }

  public void elapseSecond() {
    this.newWeatherFlag = false;
    Weather newWeather = provider.tryGetNewWeather();
    if (newWeather != null) {
      synchronized (provider) {
        currentWeather = newWeather;
        newWeatherFlag = true;
      }
    }
  }

  public Weather getWeather() {
    synchronized (provider) {
      return currentWeather;
    }
  }

  public void init() {
    this.currentWeather = provider.tryGetNewWeather();
    assert this.currentWeather != null;
  }

  public boolean isNewWeather() {
    return newWeatherFlag;
  }

  public void save(XElement target) {
    XmlSaveUtils.Field.storeField(target, this, "newWeatherFlag");
    XmlSaveUtils.Field.storeField(target, this, "weather",
            ObjectSerializer.createFor(Weather.class));

    //provider is obtained from settings
  }

  public void setWeather(String metarString) {
    try {
      Weather tmp = MetarDecoder.decode(metarString);
      setWeather(tmp);
    } catch (Exception ex) {
      //TODO Implement this: Implement this
      throw new ToDoException("Implement this");
      //Context.getShared().getSimLog().sendTextMessageForUser("Failed to decode metar. " + ex.getMessage());
    }
  }

  public void setWeather(Weather weather) {
    EAssert.Argument.isNotNull(weather);
    synchronized (provider) {
      this.currentWeather = weather;
      this.newWeatherFlag = true;
    }
  }
}
