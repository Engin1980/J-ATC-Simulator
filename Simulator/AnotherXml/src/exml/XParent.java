package exml;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;

public class XParent {

  private final IMap<Class<?>, Object> parents = new EMap<>();

  public void set(Object obj) {
    this.parents.set(obj.getClass(), obj);
  }

  public void clear(Class<?> type) {
    this.parents.tryRemove(type);
  }

  public <T> T get(Class<T> type) {
    T ret = (T) parents.get(type);
    return ret;
  }
}
