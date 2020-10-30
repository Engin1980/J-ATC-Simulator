package eng.jAtcSimLib.xmlUtils.selector;

public class TypeSerializerSelector implements SerializerSelector {
  private final Class<?> type;
  private final boolean includeSubclasses;

  public TypeSerializerSelector(Class<?> type, boolean includeSubclasses) {
    this.type = type;
    this.includeSubclasses = includeSubclasses;
  }

  @Override
  public boolean isApplicable(Object value) {
    return includeSubclasses ?
            type.isAssignableFrom(value.getClass()) : type.equals(value.getClass());
  }
}
