package eng.jAtcSim.newLib.textProcessing.parsing.shortcuts;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.eSystem.collections.ISet;

import java.util.Map;

public class ShortcutList<T> {

  private final IMap<T, T> inner = new EMap<>();

  public void add(T key, T value) {
    if (value != null) {
      inner.set(key, value);
    } else {
      remove(key);
    }
  }

  public IMap<T, T> getAll2() {
    IMap<T, T> ret = new EMap<>();
    for (Map.Entry<T, T> stringStringEntry : inner) {
      ret.set(stringStringEntry.getKey(), stringStringEntry.getValue());
    }
    return ret;
  }

  public void setAll2(IMap<T, T> shortcuts) {
    for (Map.Entry<T, T> shortcut : shortcuts) {
      inner.set(shortcut.getKey(), shortcut.getValue());
    }
  }

  public ISet<Map.Entry<T, T>> getEntries() {
    ISet<Map.Entry<T, T>> ret = inner.getEntries();
    return ret;
  }

  public void remove(T key) {
    if (inner.containsKey(key)) inner.remove(key);
  }

  public T tryGet(T key) {
    T ret = inner.tryGet(key);
    return ret;
  }
}
