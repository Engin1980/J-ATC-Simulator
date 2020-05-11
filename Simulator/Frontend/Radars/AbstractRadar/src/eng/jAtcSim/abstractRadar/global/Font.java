package eng.jAtcSim.abstractRadar.global;

public class Font {
  private final String name;
  private final int style;
  private final int size;

  public Font(String name, int size, int style) {
    this.name = name;
    this.style = style;
    this.size = size;
  }

  public String getName() {
    return name;
  }

  public int getStyle() {
    return style;
  }

  public int getSize() {
    return size;
  }
}
