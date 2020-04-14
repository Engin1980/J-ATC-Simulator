package eng.jAtcSim.newLib.area.approaches.conditions;

import eng.eSystem.collections.*;
import eng.eSystem.validation.EAssert;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class AggregatingCondition implements ICondition {
  public enum eConditionAggregator{
    and,
    or
  }

  public static AggregatingCondition create(eConditionAggregator aggregator, ICondition  ... conditions) {
    return new AggregatingCondition(EList.of(conditions), aggregator);
  }

  private final IList<ICondition> conditions;
  private final eConditionAggregator aggregator;

  public AggregatingCondition(IList<ICondition> conditions, eConditionAggregator aggregator) {
    EAssert.Argument.isNotNull(conditions, "conditions");
    this.conditions = conditions;
    this.aggregator = aggregator;
  }

  @Override
  public ICondition createCopy() {
    return new AggregatingCondition(
        this.conditions.select(q->q.createCopy()),
        this.aggregator
    );
  }

  public IList<ICondition> getConditions() {
    return conditions;
  }

  public eConditionAggregator getAggregator() {
    return aggregator;
  }
}
