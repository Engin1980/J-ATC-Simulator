package eng.jAtcSim.newLib.shared;

public class Settings {
  public String logPath;

  private boolean getWeatherInfoStringAsMetar;
  private boolean useExtendedCallsigns;
  private double delayStepProbability;
  private int delayStep;

  public Settings(boolean getWeatherInfoStringAsMetar) {
    this.getWeatherInfoStringAsMetar = getWeatherInfoStringAsMetar;
  }

  public int getDelayStep() {
    return delayStep;
  }

  public double getDelayStepProbability() {
    return delayStepProbability;
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
