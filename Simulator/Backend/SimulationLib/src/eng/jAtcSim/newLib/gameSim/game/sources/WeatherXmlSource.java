package eng.jAtcSim.newLib.gameSim.game.sources;

import eng.eSystem.validation.EAssert;
import eng.eSystem.validation.Validator;
import eng.jAtcSim.newLib.weather.PresetWeatherProvider;
import eng.jAtcSim.newLib.weather.WeatherProvider;
import java.nio.file.Paths;

public class WeatherXmlSource extends WeatherSource {

  private WeatherProvider content;
  private String xmlFileName;

  public WeatherXmlSource(String xmlFileName) {
    EAssert.Argument.isNotNull(xmlFileName, "xmlFileName");
    EAssert.isTrue(java.nio.file.Files.exists(Paths.get(xmlFileName)));
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
