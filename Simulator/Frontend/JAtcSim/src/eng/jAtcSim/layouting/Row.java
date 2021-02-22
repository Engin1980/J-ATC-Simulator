package eng.jAtcSim.layouting;

import eng.eSystem.validation.EAssert;

class Row {
  private final Value height;
  private final Block content;

  public Row(Value height, Block content) {
    EAssert.Argument.isNotNull(content);
    EAssert.Argument.isNotNull(height);

    this.content = content;
    this.height = height;
  }

  public Block getContent() {
    return content;
  }

  public Value getHeight() {
    return height;
  }
}
