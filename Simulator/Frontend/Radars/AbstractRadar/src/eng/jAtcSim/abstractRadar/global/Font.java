package eng.jAtcSim.abstractRadar.global;

public class Font {
  private String name;
  private int style;
  private int size;

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
