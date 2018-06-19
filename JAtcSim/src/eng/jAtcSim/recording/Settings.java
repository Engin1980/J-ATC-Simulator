package eng.jAtcSim.recording;

public class Settings {
  private String path;
  private int interval;
  private int width;
  private int height;
  private String imageType;
  private float jpgQuality;

  public Settings(String path, int interval, int windowWidth, int windowHeight, String imageType, float jpgQuality) {
    this.path = path;
    this.interval = interval;
    this.width = windowWidth;
    this.height = windowHeight;
    this.imageType = imageType;
    this.jpgQuality = jpgQuality;
  }

  public String getImageType() {
    return imageType;
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

  public float getJpgQuality() {
    return jpgQuality;
  }
}
