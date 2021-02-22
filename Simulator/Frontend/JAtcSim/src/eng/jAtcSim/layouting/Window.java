package eng.jAtcSim.layouting;

import eng.eSystem.validation.EAssert;

class Window {
  private final Position position;
  private final String title;
  private final Block content;

  public Window(Position position, Block content, String title) {
    EAssert.Argument.isNotNull(position, "position");
    EAssert.Argument.isNotNull(content, "content");

    this.position = position;
    this.content = content;
    this.title = title;
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
}
