package eng.jAtcSim.newLib.gameSim.game.sources;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.weather.PresetWeatherProvider;
import eng.jAtcSim.newLib.weather.WeatherProvider;
import java.nio.file.Paths;

public class WeatherXmlSource extends WeatherSource {

  private WeatherProvider content;
  private String fileName;

  WeatherXmlSource(String fileName) {
    EAssert.Argument.isNotNull(fileName, "xmlFileName");
    EAssert.isTrue(java.nio.file.Files.exists(Paths.get(fileName)));
    this.fileName = fileName;
  }

  public String getFileName() {
    return fileName;
  }

  @Override
  protected void _init() {
    content = new PresetWeatherProvider(this.fileName);
    super.setInitialized();
  }

  @Override
  protected WeatherProvider _getContent() {
    return content;
  }
}
