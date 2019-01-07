package eng.jAtcSim.lib.weathers.presets;

import eng.jAtcSim.lib.weathers.Weather;

import java.time.LocalTime;

public class PresetWeather extends Weather {
  private LocalTime time;

  public LocalTime getTime() {
    return time;
  }

  public PresetWeather(LocalTime time, int windHeading, int windSpeedInKts, int windGustSpeedInKts, int visibilityInM, int cloudBaseInFt, double cloudBaseHitProbability, eSnowState snowState) {
    super(windHeading, windSpeedInKts, windGustSpeedInKts, visibilityInM, cloudBaseInFt, cloudBaseHitProbability, snowState);
    this.time = time;
  }
}
