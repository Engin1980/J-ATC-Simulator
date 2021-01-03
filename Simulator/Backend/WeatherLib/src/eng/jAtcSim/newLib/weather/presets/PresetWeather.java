package eng.jAtcSim.newLib.weather.presets;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.PostContracts;
import eng.jAtcSim.newLib.shared.xml.SmartXmlLoaderUtils;
import eng.jAtcSim.newLib.weather.Weather;
import eng.newXmlUtils.annotations.XmlConstructor;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class PresetWeather extends Weather {
  public static PresetWeather load(XElement source) {
    SmartXmlLoaderUtils.setContext(source);
    String timeS = SmartXmlLoaderUtils.loadString("time");
    LocalTime time = LocalTime.parse(
            timeS,
            DateTimeFormatter.ofPattern("HH:mm"));
    int cloudBaseAltitude = SmartXmlLoaderUtils.loadInteger("cloudBaseAltitude");
    double cloudBaseProbability = SmartXmlLoaderUtils.loadDouble("cloudBaseProbability");
    int visibility = SmartXmlLoaderUtils.loadInteger("visibility");
    int windDirection = SmartXmlLoaderUtils.loadInteger("windDirection");
    int windSpeed = SmartXmlLoaderUtils.loadInteger("windSpeed");

    //TODO doesn't load snow state
    PresetWeather ret = new PresetWeather(time, windDirection, windSpeed,
            windSpeed, visibility, cloudBaseAltitude, cloudBaseProbability,
            eSnowState.none);
    return ret;
  }

  private LocalTime time;

  @XmlConstructor
  private PresetWeather() {
    PostContracts.register(this, () -> this.time != null);
  }

  public PresetWeather(LocalTime time, int windHeading, int windSpeedInKts, int windGustSpeedInKts, int visibilityInM, int cloudBaseInFt, double cloudBaseHitProbability, eSnowState snowState) {
    super(windHeading, windSpeedInKts, windGustSpeedInKts, visibilityInM, cloudBaseInFt, cloudBaseHitProbability, snowState);
    this.time = time;
  }

  public LocalTime getTime() {
    return time;
  }
}
