package eng.jAtcSim.lib.global.newSources;

import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.validation.Validator;
import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
import eng.jAtcSim.lib.weathers.*;
import eng.jAtcSim.lib.weathers.downloaders.MetarDownloaderNoaaGov;
import eng.jAtcSim.lib.weathers.presets.PresetWeather;

import java.nio.file.Paths;

public class XmlWeatherSource extends WeatherSource {

  @XmlIgnore
  private WeatherProvider content;
  private String xmlFileName;

  public XmlWeatherSource(String xmlFileName, Weather initialWeather) {
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
