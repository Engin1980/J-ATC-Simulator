package eng.jAtcSim.newLib.shared;

public class Settings {
  private String logPath;

  private boolean getWeatherInfoStringAsMetar;

  public Settings(boolean getWeatherInfoStringAsMetar) {
    this.getWeatherInfoStringAsMetar = getWeatherInfoStringAsMetar;
  }

  public String getLogPath() {
    return logPath;
  }

  public boolean isGetWeatherInfoStringAsMetar() {
    return getWeatherInfoStringAsMetar;
  }

  public void setGetWeatherInfoStringAsMetar(boolean getWeatherInfoStringAsMetar) {
    this.getWeatherInfoStringAsMetar = getWeatherInfoStringAsMetar;
  }
}
