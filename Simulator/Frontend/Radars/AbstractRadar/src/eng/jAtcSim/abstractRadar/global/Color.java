package eng.jAtcSim.abstractRadar.global;

public class Color {
  public static final Color MAGENTA = new Color(0xFF, 0x0, 0xFF);
  private final int r;
  private final int g;
  private final int b;

  public Color(){
    this.r = 0;
    this.g = 0;
    this.b = 0;
  }

  public Color(int r, int g, int b) {
    this.r = r;
    this.g = g;
    this.b = b;
  }

  public int getR() {
    return r;
  }

  public int getG() {
    return g;
  }

  public int getB() {
    return b;
  }

  @Override
  public String toString() {
    return
        String.format("RadarColor{%d,%d,%d}", r, g, b);
  }
}
