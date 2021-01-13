package eng.jAtcSim.newLib.area.approaches.conditions;

import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.area.approaches.perCategoryValues.IntegerPerCategoryValue;
import eng.jAtcSim.newLib.shared.PostContracts;
import eng.newXmlUtils.annotations.XmlConstructor;
import exml.annotations.XConstructor;

public class PlaneShaCondition implements ICondition {

  public enum eType {
    heading,
    speed,
    altitude
  }

  public static PlaneShaCondition create(eType type, Integer minimum, Integer maximum) {
    IntegerPerCategoryValue ipcvMin = minimum == null ? null : IntegerPerCategoryValue.create(minimum);
    IntegerPerCategoryValue ipcvMax = maximum == null ? null : IntegerPerCategoryValue.create(maximum);
    PlaneShaCondition ret = create(type, ipcvMin, ipcvMax);
    return ret;
  }

  public static PlaneShaCondition create(eType type, IntegerPerCategoryValue minimum, IntegerPerCategoryValue maximum) {
    return new PlaneShaCondition(type, minimum, maximum);
  }

  private final IntegerPerCategoryValue minimum;
  private final IntegerPerCategoryValue maximum;
  private final eType type;

  @XConstructor
  @XmlConstructor
  private PlaneShaCondition() {
    this.minimum = null;
    this.maximum = null;
    this.type = eType.speed;

    PostContracts.register(this, () -> this.minimum != null || this.maximum != null);
  }

  private PlaneShaCondition(eType type, IntegerPerCategoryValue minimum, IntegerPerCategoryValue maximum) {
    switch (type) {
      case altitude:
      case speed:
        EAssert.Argument.isTrue(minimum != null || maximum != null);
        break;
      case heading:
        EAssert.Argument.isTrue(minimum != null && maximum != null);
        break;
      default:
        throw new EEnumValueUnsupportedException(type);
    }

    this.type = type;
    this.minimum = minimum;
    this.maximum = maximum;
  }

  public IntegerPerCategoryValue getMaximum() {
    return maximum;
  }

  public IntegerPerCategoryValue getMinimum() {
    return minimum;
  }

  public eType getType() {
    return type;
  }

  @Override
  public String toString() {
    return "PlaneShaCondition{" +
            this.type.toString() + " :: " + minimum + " ... " + maximum + "}";
  }
}
