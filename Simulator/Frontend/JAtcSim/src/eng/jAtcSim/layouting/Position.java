package eng.jAtcSim.layouting;

import eng.eSystem.validation.EAssert;

class Position {
  private final Value x;
  private final Value y;
  private final Value width;
  private final Value height;
  private final Integer monitor;

  public Position(Value x, Value y, Value width, Value height, Integer monitor) {
    EAssert.Argument.isNotNull(x, "x");
    EAssert.Argument.isNotNull(y, "y");
    EAssert.Argument.isNotNull(width, "width");
    EAssert.Argument.isNotNull(height, "height");

    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    this.monitor = monitor;
  }

  public Position(Value x, Value y, Value width, Value height) {
    this(x, y, width, height, 1);
  }

  public Value getX() {
    return x;
  }

  public Value getY() {
    return y;
  }

  public Value getWidth() {
    return width;
  }

  public Value getHeight() {
    return height;
  }

  public Integer getMonitor() {
    return monitor;
  }
}
