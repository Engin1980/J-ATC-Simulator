package eng.jAtcSim.layouting;

import eng.eSystem.validation.EAssert;

public class Column {
  private final Value width;
  private final Block content;

  public Column(Value width, Block content) {
    EAssert.Argument.isNotNull(content);
    EAssert.Argument.isNotNull(width);

    this.content = content;
    this.width = width;
  }

  public Value getWidth() {
    return width;
  }

  public Block getContent() {
    return content;
  }

}
