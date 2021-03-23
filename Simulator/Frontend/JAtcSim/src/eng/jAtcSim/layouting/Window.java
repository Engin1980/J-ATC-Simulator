package eng.jAtcSim.layouting;

import eng.eSystem.validation.EAssert;

class Window {

  public enum WindowStyle{
    minimized,
    normal,
    maximized;

    public static WindowStyle parse(String text) {
      if (text.toLowerCase().trim().equals("minimized"))
        return minimized;
      else if (text.toLowerCase().trim().equals("maximized"))
        return maximized;
      else if (text.toLowerCase().trim().equals("normal"))
        return normal;
      else
        throw new IllegalArgumentException("Invalid value " + text);
    }
  }

  private final WindowStyle style;
  private final Position position;
  private final String title;
  private final Block content;
  private final boolean withMenu;

  public Window(Position position, Block content, String title, WindowStyle style, boolean withMenu) {
    EAssert.Argument.isNotNull(position, "position");
    EAssert.Argument.isNotNull(content, "content");

    this.position = position;
    this.content = content;
    this.title = title;
    this.withMenu = withMenu;
    this.style = style;
  }

  public WindowStyle getStyle() {
    return style;
  }

  public Block getContent() {
    return content;
  }

  public Position getPosition() {
    return position;
  }

  public String getTitle() {
    return title;
  }

  public boolean isWithMenu() {
    return withMenu;
  }
}
