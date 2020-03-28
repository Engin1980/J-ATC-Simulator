package eng.jAtcSim.newLib.weather.presets;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;
import eng.jAtcSim.newLib.weather.Weather;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class PresetWeather extends Weather {
  public static PresetWeather load(XElement source) {
    XmlLoaderUtils.setContext(source);
    String timeS = XmlLoaderUtils.loadString("time");
    LocalTime time = LocalTime.parse(
        timeS,
        DateTimeFormatter.ofPattern("HH:mm"));
    int cloudBaseAltitude = XmlLoaderUtils.loadInteger("cloudBaseAltitude");
    double cloudBaseProbability = XmlLoaderUtils.loadDouble("cloudBaseProbability");
    int visibility = XmlLoaderUtils.loadInteger("visibility");
    int windDirection = XmlLoaderUtils.loadInteger("windDirection");
    int windSpeed = XmlLoaderUtils.loadInteger("windSpeed");

    //TODO doesn't load snow state
    PresetWeather ret = new PresetWeather(time, windDirection, windSpeed,
        windSpeed, visibility, cloudBaseAltitude, cloudBaseProbability,
        eSnowState.none);
    return ret;
  }

  private LocalTime time;

  public PresetWeather(LocalTime time, int windHeading, int windSpeedInKts, int windGustSpeedInKts, int visibilityInM, int cloudBaseInFt, double cloudBaseHitProbability, eSnowState snowState) {
    super(windHeading, windSpeedInKts, windGustSpeedInKts, visibilityInM, cloudBaseInFt, cloudBaseHitProbability, snowState);
    this.time = time;
  }

  public LocalTime getTime() {
    return time;
  }
}
