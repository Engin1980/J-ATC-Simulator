package exml;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class Values {

  private final IMap<Class<?>, Object> typeInner = new EMap<>();

  public void clear(Class<?> type) {
    this.typeInner.tryRemove(type);
  }

  public <T> T get(Class<T> type) {

    T ret;
    try {
      ret = (T) typeInner.get(type);
    } catch (Exception e) {
      throw new IllegalArgumentException(sf("There is no value for type '%s'.", type.getName()), e);
    }
    return ret;
  }

  public void set(Object obj) {
    this.typeInner.set(obj.getClass(), obj);
  }

  public <T> void set(Class<? extends T> type, T value) {
    this.typeInner.set(type, value);
  }
}
