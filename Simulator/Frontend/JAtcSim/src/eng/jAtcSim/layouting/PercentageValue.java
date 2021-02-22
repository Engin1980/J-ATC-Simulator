package eng.jAtcSim.layouting;

import eng.eSystem.utilites.NumberUtils;
import eng.eSystem.validation.EAssert;

class PercentageValue extends Value {
  private final int value;

  public PercentageValue(int value) {
    EAssert.Argument.isTrue(NumberUtils.isBetweenOrEqual(0, value, 100));
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
