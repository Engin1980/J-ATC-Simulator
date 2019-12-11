package eng.jAtcSim.newLib.shared;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.eSystem.exceptions.EApplicationException;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class Factory {

  private static final IMap<Class<?>, Object> classInstances = new EMap<>();
  private static final IMap<Class<?>, IMap<Object, Object>> classKeyInstancew = new EMap<>();

  private Factory() {
  }

  public static <T> T getInstance(Class<? extends T> type) {
    if (classKeyInstancew.containsKey(type) == false) {
      throw new EApplicationException(sf("Type '%s' not stored in the Factory.", type.getName()));
    }
    T ret = (T) classInstances.get(type);
    return ret;
  }

  public static <T> T getInstance(Class<? extends T> type, Object key) {
    if (classKeyInstancew.containsKey(type) == false) {
      throw new EApplicationException(sf("Type '%s' not stored in the Factory.", type.getName()));
    }
    IMap<Object, Object> in = classKeyInstancew.get(type);
    if (in.containsKey(key) == false) {
      throw new EApplicationException(sf("Type '%s' with key object '%s' {%s} not stored in the Factory.",
          type.getName(), key.toString(), key.getClass().getName()));
    }
    T ret = (T) in.get(key);
    return ret;
  }

  public static <T> void setInstance(T value) {
    classInstances.set(value.getClass(), value);
  }

  public static <T> void setInstance(Object key, T value) {
    if (key == null) {
      throw new EApplicationException(sf("Null key cannot be used for storing type '%s'.", value.getClass().getName()));
    }
    if (value == null)
      throw new EApplicationException(sf("Null value cannot be stored under key '%s'.", key));
    IMap<Object, Object> in = classKeyInstancew.getOrSet(value.getClass(), new EMap<>());
    in.set(key, value);
  }
}
