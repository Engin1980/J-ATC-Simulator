package eng.jAtcSim.newLib.area.approaches.conditions;

import eng.eSystem.collections.*;
import eng.eSystem.validation.EAssert;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class AggregatingCondition {
  public enum eConditionAggregator{
    and,
    or
  }

  private final IList<ICondition> conditions;
  private final eConditionAggregator aggregator;

  public AggregatingCondition(IList<ICondition> conditions, eConditionAggregator aggregator) {
    EAssert.Argument.isNotNull(conditions, "conditions");
    this.conditions = conditions;
    this.aggregator = aggregator;
  }

  public IList<ICondition> getConditions() {
    return conditions;
  }

  public eConditionAggregator getAggregator() {
    return aggregator;
  }
}
