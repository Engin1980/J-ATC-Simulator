package eng.jAtcSim.recording;

public class Settings {
  private String path;
  private int interval;
  private int width;
  private int height;
  private String imageType;

  public String getImageType() {
    return imageType;
  }

  public Settings(String path, int interval, int windowWidth, int windowHeight, String imageType) {
    this.path = path;
    this.interval = interval;
    this.width = windowWidth;
    this.height = windowHeight;
    this.imageType = imageType;
  }

  public String getPath() {
    return path;
  }

  public int getInterval() {
    return interval;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }
}
