package eng.jAtcSim.lib.weathers;

import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.xmlSerialization.XmlSerializer;
import eng.eSystem.xmlSerialization.XmlSettings;
import eng.jAtcSim.lib.weathers.presets.PresetWeatherList;

public class PresetWeatherProvider extends WeatherProvider {

  private PresetWeatherList presetWeathers;

  public PresetWeatherProvider(String sourceFileName) {

    XmlSettings sett = new XmlSettings();
    XmlSerializer ser = new XmlSerializer(sett);

    try {
      presetWeathers = ser.deserialize(sourceFileName, PresetWeatherList.class);
    } catch ( Exception ex){
      throw new EApplicationException("Failed to load preset weather from " + sourceFileName + ".", ex);
    }

  }

  @Override
  public Weather tryGetNewWeather() {
    return null;
  }
}
