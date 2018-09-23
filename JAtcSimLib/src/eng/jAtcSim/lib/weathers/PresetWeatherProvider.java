package eng.jAtcSim.lib.weathers;

import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.xmlSerialization.XmlSerializer;
import eng.eSystem.xmlSerialization.XmlSettings;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.weathers.presets.PresetWeather;
import eng.jAtcSim.lib.weathers.presets.PresetWeatherList;

public class PresetWeatherProvider extends WeatherProvider {

  private PresetWeatherList presetWeathers;
  private int nextAvailableWeatherIndex = -1;

  public PresetWeatherProvider(String sourceFileName) {

    XmlSettings sett = new XmlSettings();
    XmlSerializer ser = new XmlSerializer(sett);

    try {
      presetWeathers = ser.deserialize(sourceFileName, PresetWeatherList.class);
    } catch (Exception ex) {
      throw new EApplicationException("Failed to load preset weather from " + sourceFileName + ".", ex);
    }

    if (presetWeathers.size() == 0)
      throw new EApplicationException("There is no weather descriptions specified in the file " + sourceFileName + ".");

    presetWeathers.sort();
  }

  @Override
  public Weather tryGetNewWeather() {
    Weather ret = null;
    if (nextAvailableWeatherIndex == -1) {
      int index = getFirstWeatherIndex();
      ret = presetWeathers.get(index);
      nextAvailableWeatherIndex = index+1;
    } else {
      if (nextAvailableWeatherIndex == presetWeathers.size())
        nextAvailableWeatherIndex = 0;
      PresetWeather pw = presetWeathers.get(nextAvailableWeatherIndex);
      java.time.LocalTime now = Acc.now().toLocalTime();
      if (pw.getTime().isBefore(now)) {
        ret = presetWeathers.get(nextAvailableWeatherIndex);
        nextAvailableWeatherIndex++;
      }
    }
    return ret;
  }

  private int getFirstWeatherIndex() {
    if (presetWeathers.size() == 1)
      return 0;

    int index = -1;
    java.time.LocalTime now = Acc.now().toLocalTime();
    for (int i = 0; i < presetWeathers.size(); i++) {
      if (presetWeathers.get(i).getTime().isAfter(now)){
        index = i-1;
        break;
      }
    }
    if (index == -1)
      index = presetWeathers.size()-1;

    return index;
  }
}
