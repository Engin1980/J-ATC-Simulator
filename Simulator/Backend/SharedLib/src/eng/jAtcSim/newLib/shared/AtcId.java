package eng.jAtcSim.newLib.shared;

import eng.eSystem.utilites.NumberUtils;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.enums.AtcType;

public class AtcId {
  private final String name;
  private final double frequency;
  private final AtcType type;

  public AtcId(String name, double frequency, AtcType type) {
    EAssert.Argument.isNonemptyString(name, "name");
    EAssert.Argument.isTrue(NumberUtils.isBetweenOrEqual(117, frequency, 140));
    this.name = name;
    this.frequency = frequency;
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public double getFrequency() {
    return frequency;
  }

  public AtcType getType() {
    return type;
  }

  @Override
  public String toString() {
    return name;
  }
}
