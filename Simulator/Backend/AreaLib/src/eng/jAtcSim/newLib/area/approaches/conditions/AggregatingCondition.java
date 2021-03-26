package eng.jAtcSim.newLib.area.approaches.conditions;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.PostContracts;

import exml.annotations.XConstructor;

public class AggregatingCondition implements ICondition {
  public enum eConditionAggregator {
    and,
    or
  }

  public static AggregatingCondition create(eConditionAggregator aggregator, ICondition... conditions) {
    return new AggregatingCondition(EList.of(conditions), aggregator);
  }

  private final IList<ICondition> conditions;
  private final eConditionAggregator aggregator;

  @XConstructor

  private AggregatingCondition() {
    this.conditions = null;
    this.aggregator = eConditionAggregator.or;

    PostContracts.register(this, () -> this.conditions != null);
  }

  public AggregatingCondition(IList<ICondition> conditions, eConditionAggregator aggregator) {
    EAssert.Argument.isNotNull(conditions, "conditions");
    this.conditions = conditions;
    this.aggregator = aggregator;
  }

  public eConditionAggregator getAggregator() {
    return aggregator;
  }

  public IList<ICondition> getConditions() {
    return conditions;
  }

  @Override
  public String toString() {
    return "AggregatingCondition{" +
            "aggregator=" + aggregator +
            '}';
  }
}
