package eng.jAtcSim.abstractRadar.global;

public class Font {
  private final String family;
  private final int style;
  private final int size;

  public Font(String family, int size, int style) {
    this.family = family;
    this.style = style;
    this.size = size;
  }

  public String getFamily() {
    return family;
  }

  public int getStyle() {
    return style;
  }

  public int getSize() {
    return size;
  }
}
