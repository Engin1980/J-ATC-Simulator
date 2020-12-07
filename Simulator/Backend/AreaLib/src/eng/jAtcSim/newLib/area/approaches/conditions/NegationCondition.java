package eng.jAtcSim.newLib.area.approaches.conditions;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.PostContracts;
import eng.newXmlUtils.annotations.XmlConstructor;

public class NegationCondition implements ICondition {

  public static NegationCondition create(ICondition condition) {
    return new NegationCondition(condition);
  }

  private final ICondition condition;

  @XmlConstructor
  private NegationCondition(){
    this.condition = null;
    PostContracts.register(this, () -> condition != null);
  }

  private NegationCondition(ICondition condition) {
    EAssert.Argument.isNotNull(condition, "condition");
    this.condition = condition;
  }

  public ICondition getCondition() {
    return condition;
  }

  @Override
  public String toString() {
    return "NegationCondition{" +
            "condition=" + condition.toString() +
            '}';
  }
}
