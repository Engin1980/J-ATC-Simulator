package eng.jAtcSim.recording;

public class Settings {
  private final String path;
  private final int interval;
  private final int width;
  private final int height;
  private final String imageType;
  private final float jpgQuality;

  public Settings(String path, int interval, int windowWidth, int windowHeight, String imageType, float jpgQuality) {
    this.path = path;
    this.interval = interval;
    this.width = windowWidth;
    this.height = windowHeight;
    this.imageType = imageType;
    this.jpgQuality = jpgQuality;
  }

  public int getHeight() {
    return height;
  }

  public String getImageType() {
    return imageType;
  }

  public int getInterval() {
    return interval;
  }

  public float getJpgQuality() {
    return jpgQuality;
  }

  public String getPath() {
    return path;
  }

  public int getWidth() {
    return width;
  }
}
