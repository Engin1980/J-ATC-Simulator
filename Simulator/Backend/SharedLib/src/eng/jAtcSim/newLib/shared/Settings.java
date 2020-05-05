package eng.jAtcSim.newLib.shared;

public class Settings {
  public String logPath;

  private boolean getWeatherInfoStringAsMetar;
  private boolean useExtendedCallsigns;

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

  public boolean isUseExtendedCallsigns() {
    return useExtendedCallsigns;
  }
}
