package eng.jAtcSim.lib.weathers;

import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.xmlSerialization.XmlSerializer;
import eng.eSystem.xmlSerialization.XmlSettings;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.weathers.presets.PresetWeather;
import eng.jAtcSim.lib.weathers.presets.PresetWeatherList;

public class PresetWeatherProvider extends WeatherProvider {

  private PresetWeatherList presetWeathers;
  private int dayIndex = -1;
  private int weatherIndex = -1;

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
    if (dayIndex != Acc.now().getDays()) {
      int index = getFirstWeatherIndex();
      ret = presetWeathers.get(index);
      weatherIndex = index;
      dayIndex = Acc.now().getDays();
    } else {
      if (weatherIndex == presetWeathers.size())
        ret = null; // no more weahter available
      else {
        java.time.LocalTime now = Acc.now().toLocalTime();
        int newIndex = weatherIndex;
        while (true) {
          if (newIndex == presetWeathers.size()) {
            newIndex = -1;
            break;
          } else if (presetWeathers.get(newIndex).getTime().isBefore(now)) {
            newIndex++;
          } else {
            break;
          }
        }
        if (newIndex == -1) {
          weatherIndex = presetWeathers.size();
          ret = null;
        } else if (newIndex == weatherIndex) {
          ret = null;
        } else {
          ret = presetWeathers.get(newIndex);
          weatherIndex = newIndex;
        }
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
      if (presetWeathers.get(i).getTime().isAfter(now)) {
        index = i - 1;
        break;
      }
    }
    if (index == -1)
      index = presetWeathers.size() - 1;

    return index;
  }
}
