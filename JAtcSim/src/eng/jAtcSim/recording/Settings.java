package eng.jAtcSim.recording;

public class Settings {
  private String path;
  private int interval;

  public Settings(String path, int interval) {
    this.path = path;
    this.interval = interval;
  }

  public String getPath() {
    return path;
  }

  public int getInterval() {
    return interval;
  }
}
