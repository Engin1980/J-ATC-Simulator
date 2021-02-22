package eng.jAtcSim.layouting;

import eng.eSystem.validation.EAssert;

class PixelValue extends Value {
  private final int value;

  public PixelValue(int value) {
    EAssert.Argument.isTrue(value >= 0);
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
