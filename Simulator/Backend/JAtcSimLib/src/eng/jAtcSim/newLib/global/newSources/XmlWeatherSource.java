package eng.jAtcSim.newLib.global.newSources;

import eng.eSystem.validation.Validator;
;
import eng.eSystem.xmlSerialization.annotations.XmlConstructor;
import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
import eng.jAtcSim.newLib.weathers.PresetWeatherProvider;
import eng.jAtcSim.newLib.weathers.Weather;
import eng.jAtcSim.newLib.weathers.WeatherProvider;

import java.nio.file.Paths;

public class XmlWeatherSource extends WeatherSource {

  @XmlIgnore
  private WeatherProvider content;
  private String xmlFileName;

  @XmlConstructor
  private XmlWeatherSource(){

  }

  public XmlWeatherSource(String xmlFileName) {
    Validator.isNotNull(xmlFileName);
    Validator.check(java.nio.file.Files.exists(Paths.get(xmlFileName)));
    this.xmlFileName = xmlFileName;
  }

  @Override
  public void init() {
    content = new PresetWeatherProvider(this.xmlFileName);
    super.setInitialized();
  }

  @Override
  protected WeatherProvider _getContent() {
    return content;
  }
}
