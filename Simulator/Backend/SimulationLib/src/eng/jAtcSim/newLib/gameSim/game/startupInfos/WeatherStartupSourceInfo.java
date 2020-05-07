package eng.jAtcSim.newLib.gameSim.game.startupInfos;

import eng.eSystem.collections.*;
import eng.jAtcSim.newLib.weather.Weather;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class WeatherStartupSourceInfo {
  public enum WeatherSourceType {
    xml,
    user,
    online
  }

  public String weatherXmlFile;
  public Weather initialWeather;
  public WeatherSourceType weatherProviderType;
}
