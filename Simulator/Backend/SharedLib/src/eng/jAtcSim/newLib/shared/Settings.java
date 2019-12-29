package eng.jAtcSim.newLib.shared;

public class Settings {
  private boolean getWeatherInfoStringAsMetar;

  public Settings(boolean getWeatherInfoStringAsMetar) {
    this.getWeatherInfoStringAsMetar = getWeatherInfoStringAsMetar;
  }

  public boolean isGetWeatherInfoStringAsMetar() {
    return getWeatherInfoStringAsMetar;
  }

  public void setGetWeatherInfoStringAsMetar(boolean getWeatherInfoStringAsMetar) {
    this.getWeatherInfoStringAsMetar = getWeatherInfoStringAsMetar;
  }
}
