package eng.jAtcSim.newLib.area.weathers.presets;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.weathers.Weather;
import eng.jAtcSim.newLib.world.xml.XmlLoader;
import sun.plugin2.main.server.WindowsHelper;

import java.time.LocalTime;

public class PresetWeather extends Weather {
  public static PresetWeather load(XElement source) {
    XmlLoader.setContext(source);
    String timeS = XmlLoader.loadString("time");
    LocalTime time = new LocalTimeParser().parse(timeS);
    int cloudBaseAltitude = XmlLoader.loadInteger("cloudBaseAltitude");
    double cloudBaseProbability = XmlLoader.loadDouble("cloudBaseProbability");
    int visibility = XmlLoader.loadInteger("visibility");
    int windDirection = XmlLoader.loadInteger("windDirection");
    int windSpeed = XmlLoader.loadInteger("windSpeed");

    //TODO doesn't load snow state
    PresetWeather ret = new PresetWeather(time, windDirection, windSpeed,
        windSpeed, visibility, cloudBaseAltitude, cloudBaseProbability,
        eSnowState.none);
    return ret;
  }

  private LocalTime time;

  public LocalTime getTime() {
    return time;
  }

  public PresetWeather(LocalTime time, int windHeading, int windSpeedInKts, int windGustSpeedInKts, int visibilityInM, int cloudBaseInFt, double cloudBaseHitProbability, eSnowState snowState) {
    super(windHeading, windSpeedInKts, windGustSpeedInKts, visibilityInM, cloudBaseInFt, cloudBaseHitProbability, snowState);
    this.time = time;
  }
}
