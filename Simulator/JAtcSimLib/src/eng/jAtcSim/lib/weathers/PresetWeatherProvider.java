package eng.jAtcSim.lib.weathers;

import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.xmlSerialization.XmlSerializer;
import eng.eSystem.xmlSerialization.XmlSettings;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.weathers.presets.PresetWeather;
import eng.jAtcSim.lib.weathers.presets.PresetWeatherList;

import java.util.prefs.NodeChangeEvent;

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
    if (dayIndex == -1) {
      int index = getFirstWeatherIndex();
      ret = presetWeathers.get(index);
      weatherIndex = index;
      dayIndex = Acc.now().getDays();
    } else {
      if (dayIndex == Acc.now().getDays()) {
        // evaluating current day
        int newIndex = getSuggestedWeatherIndex();
        if (newIndex == NEXT_DAY) {
          dayIndex++;
          weatherIndex = -1;
        } else if (newIndex != NO_CHANGE) {
          ret = presetWeathers.get(newIndex);
          weatherIndex = newIndex;
        } else {
          ret = null;
        }
      } else
        // waiting for the next day
        ret = null;
    }
    return ret;
  }

  private static final int NO_CHANGE = -2;
  private static final int NEXT_DAY = -1;
  private int getSuggestedWeatherIndex() {
    int ret;
    java.time.LocalTime now = Acc.now().toLocalTime();

    if (weatherIndex + 1 == presetWeathers.size())
      ret = NEXT_DAY;
    else {
      int suggestedIndex = NO_CHANGE;
      for (int i = weatherIndex + 1; i < presetWeathers.size(); i++) {
        if (presetWeathers.get(i).getTime().isBefore(now))
          suggestedIndex = i;
      }
      ret = suggestedIndex;
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
