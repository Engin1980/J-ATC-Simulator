package eng.jAtcSim.newLib.shared;

import eng.eSystem.utilites.NumberUtils;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.enums.eAtcType;

public class AtcId {
  private final String id;
  private final double frequency;
  private final eAtcType atcType;

  public AtcId(String id, double frequency, eAtcType atcType) {
    EAssert.Argument.isNonemptyString(id, "id");
    EAssert.Argument.isTrue(NumberUtils.isBetweenOrEqual(117, frequency, 140));
    this.id = id;
    this.frequency = frequency;
    this.atcType = atcType;
  }

  public String getId() {
    return id;
  }

  public double getFrequency() {
    return frequency;
  }

  public eAtcType getAtcType() {
    return atcType;
  }
}
