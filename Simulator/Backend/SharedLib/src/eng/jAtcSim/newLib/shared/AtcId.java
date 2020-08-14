package eng.jAtcSim.newLib.shared;

import eng.eSystem.utilites.NumberUtils;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.enums.AtcType;

import java.util.Objects;

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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AtcId atcId = (AtcId) o;
    return name.equals(atcId.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }
}
