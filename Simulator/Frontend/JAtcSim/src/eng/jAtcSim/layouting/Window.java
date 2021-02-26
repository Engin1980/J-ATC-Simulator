package eng.jAtcSim.layouting;

import eng.eSystem.validation.EAssert;

class Window {
  private final Position position;
  private final String title;
  private final Block content;
  private final boolean withMenu;

  public Window(Position position, Block content, String title, boolean withMenu) {
    EAssert.Argument.isNotNull(position, "position");
    EAssert.Argument.isNotNull(content, "content");

    this.position = position;
    this.content = content;
    this.title = title;
    this.withMenu = withMenu;
  }

  public String getTitle() {
    return title;
  }

  public Block getContent() {
    return content;
  }

  public Position getPosition() {
    return position;
  }

  public boolean isWithMenu() {
    return withMenu;
  }
}
