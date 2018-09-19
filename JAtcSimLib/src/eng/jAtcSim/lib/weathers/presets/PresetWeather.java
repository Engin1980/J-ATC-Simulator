package eng.jAtcSim.lib.weathers.presets;

import eng.jAtcSim.lib.weathers.Weather;

import java.time.LocalTime;

public class PresetWeather extends Weather {
  private LocalTime time;

  public LocalTime getTime() {
    return time;
  }

  public PresetWeather(LocalTime time, int windHeading, int windSpeetInKts, int visibilityInM, int cloudBaseInFt, double cloudBaseHitProbability) {
    super(windHeading, windSpeetInKts, visibilityInM, cloudBaseInFt, cloudBaseHitProbability);
    this.time = time;
  }
}
