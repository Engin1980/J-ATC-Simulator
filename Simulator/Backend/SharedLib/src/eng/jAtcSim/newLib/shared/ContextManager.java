package eng.jAtcSim.newLib.shared;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.eSystem.exceptions.ApplicationException;
import eng.eSystem.validation.EAssert;

public class ContextManager {
  private static final IMap<Class<?>, Object> inner = new EMap<>();

  public static void clearContext(Class<?> contextInterface) {
    EAssert.Argument.isNotNull(contextInterface, "contextClass");
    inner.tryRemove(contextInterface);
  }

  @SuppressWarnings("unchecked")
  public static <T> T getContext(Class<T> contextInterface) {
    if (inner.containsKey(contextInterface) == false)
      throw new ApplicationException("Context for class " + contextInterface.getSimpleName() + " not set.");
    return (T) inner.get(contextInterface);
  }

  public static <T> void setContext(Class<T> contextInterface, T context) {
    EAssert.Argument.isNotNull(context, "context");
    inner.set(contextInterface, context);
  }
}
