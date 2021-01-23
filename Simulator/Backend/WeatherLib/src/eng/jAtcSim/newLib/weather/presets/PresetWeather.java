package eng.jAtcSim.newLib.weather.presets;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.PostContracts;
import eng.jAtcSim.newLib.shared.xml.SmartXmlLoaderUtils;
import eng.jAtcSim.newLib.weather.Weather;
import eng.newXmlUtils.annotations.XmlConstructor;
import exml.XContext;
import exml.annotations.XConstructor;
import exml.annotations.XIgnored;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class PresetWeather extends Weather {
  private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");

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
  @XIgnored
  private LocalTime time;

  @XConstructor
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

  @Override
  public void load(XElement elm, XContext ctx) {
    this.time = LocalTime.parse(elm.getAttribute("localTime"), dtf);
  }

  @Override
  public void save(XElement elm, XContext ctx) {
    elm.setAttribute("localTime", time.format(dtf));
  }
}
