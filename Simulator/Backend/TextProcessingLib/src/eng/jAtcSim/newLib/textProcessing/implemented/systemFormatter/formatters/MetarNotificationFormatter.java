package eng.jAtcSim.newLib.textProcessing.implemented.systemFormatter.formatters;

import eng.eSystem.EStringBuilder;
import eng.jAtcSim.newLib.shared.SharedAcc;
import eng.jAtcSim.newLib.speeches.system.system2user.MetarNotification;
import eng.jAtcSim.newLib.textProcessing.implemented.formatterHelpers.SmartTextSpeechFormatter;
import eng.jAtcSim.newLib.weather.Weather;
import eng.jAtcSim.newLib.weather.WeatherAcc;

public class MetarNotificationFormatter extends SmartTextSpeechFormatter<MetarNotification> {

  //TODO put to some global settings?
  private static final boolean WEATHER_INFO_STRING_AS_METAR = true;

  @Override
  protected String _format(MetarNotification input) {
    String ret;
    if (WEATHER_INFO_STRING_AS_METAR) {
      ret = toMetarString(WeatherAcc.getWeather());
    } else {
      ret = toWeatherString(WeatherAcc.getWeather());
    }
    if (input.isUpdated())
      ret = "Weather updated. " + ret;
    return ret;
  }

  private String toMetarString(Weather weather) {
    EStringBuilder sb = new EStringBuilder();
    sb.append("METAR ");
    sb.append("???? ");
    sb.appendFormat("%02d", java.time.LocalDate.now().getDayOfMonth());
    sb.appendFormat("%02d%02dZ ", SharedAcc.getNow().getHours(), SharedAcc.getNow().getMinutes());
    if (weather.getWindSpeetInKts() == weather.getWindGustSpeedInKts())
      sb.appendFormat("%03d%02dKT ", weather.getWindHeading(), weather.getWindSpeetInKts());
    else
      sb.appendFormat("%03d%02G%02KT ", weather.getWindHeading(), weather.getWindSpeetInKts(), weather.getWindGustSpeedInKts());
    sb.appendFormat("%04d ", weather.getVisibilityInMeters());
    if (weather.getSnowState() == Weather.eSnowState.normal)
      sb.append("SN ");
    else if (weather.getSnowState() == Weather.eSnowState.intensive)
      sb.append("+SN ");
    if (weather.getCloudBaseHitProbability() == 0) {
      sb.append("SKC");
    } else {
      if (weather.getCloudBaseHitProbability() < 2d / 8)
        sb.append("FEW");
      else if (weather.getCloudBaseHitProbability() < 5d / 8)
        sb.append("BKN");
      else if (weather.getCloudBaseHitProbability() < 7d / 8)
        sb.append("SCT");
      else
        sb.append("OVC");
      sb.appendFormat("%03d", weather.getCloudBaseInFt() / 100);
    }
    sb.append(" ...");
    return sb.toString();
  }

  private String toWeatherString(Weather weather) {
    EStringBuilder sb = new EStringBuilder();
    sb.appendFormatLine("Wind %d° at %d (%d) kts, visibility %1.0f miles, cloud base at %d ft at %1.0f %%.",
        weather.getWindHeading(),
        weather.getWindSpeetInKts(),
        weather.getWindGustSpeedInKts(),
        weather.getVisibilityInMiles(),
        weather.getCloudBaseInFt(),
        weather.getCloudBaseHitProbability() * 100
    );
    if (weather.getSnowState() == Weather.eSnowState.normal)
      sb.append(" Snowing.");
    else if (weather.getSnowState() == Weather.eSnowState.intensive)
      sb.append(" Intensive snowing.");
    return sb.toString();
  }
}
