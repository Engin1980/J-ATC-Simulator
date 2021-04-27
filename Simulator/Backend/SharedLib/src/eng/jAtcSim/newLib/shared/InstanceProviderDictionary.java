package eng.jAtcSim.newLib.shared;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.eSystem.exceptions.ApplicationException;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class InstanceProviderDictionary {

  private static final IMap<Class<?>, Object> classInstances = new EMap<>();
  private static final IMap<Class<?>, IMap<Object, Object>> classKeyInstancew = new EMap<>();

  public static <T> T getInstance(Class<? extends T> type) {
    if (classKeyInstancew.containsKey(type) == false) {
      throw new ApplicationException(sf("Type '%s' not stored in the Factory.", type.getName()));
    }
    T ret = (T) classInstances.get(type);
    return ret;
  }

  public static <T> T getInstance(Class<? extends T> type, Object key) {
    if (classKeyInstancew.containsKey(type) == false) {
      throw new ApplicationException(sf("Type '%s' not stored in the Factory.", type.getName()));
    }
    IMap<Object, Object> in = classKeyInstancew.get(type);
    if (in.containsKey(key) == false) {
      throw new ApplicationException(sf("Type '%s' with key object '%s' {%s} not stored in the Factory.",
              type.getName(), key.toString(), key.getClass().getName()));
    }
    T ret = (T) in.get(key);
    return ret;
  }

  public static <T> void setInstance(Class<? extends T> type, T value) {
    classInstances.set(type, value);
  }

  public static <T> void setInstance(Class<? extends T> type, Object key, T value) {
    if (key == null) {
      throw new ApplicationException(sf("Null key cannot be used for storing type '%s'.", value.getClass().getName()));
    }
    if (value == null)
      throw new ApplicationException(sf("Null value cannot be stored under key '%s'.", key));
    IMap<Object, Object> in = classKeyInstancew.getOrSet(type, new EMap<>());
    in.set(key, value);
  }

  private InstanceProviderDictionary() {
  }
}
