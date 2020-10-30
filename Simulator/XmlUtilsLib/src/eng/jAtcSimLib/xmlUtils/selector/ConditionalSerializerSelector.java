package eng.jAtcSimLib.xmlUtils.selector;

import eng.eSystem.functionalInterfaces.Predicate;
import eng.eSystem.functionalInterfaces.Selector;

public class ConditionalSerializerSelector implements SerializerSelector {
  private final Predicate<Object> condition;

  public ConditionalSerializerSelector(Predicate<Object> condition) {
    this.condition = condition;
  }

  @Override
  public boolean isApplicable(Object value) {
    return condition.invoke(value);
  }
}
