package eng.jAtcSim.newLib.area.textProcessing.parsing;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.eSystem.collections.ISet;

import java.util.Map;

public class ShortcutList {

  private IMap<String, String> inner = new EMap<>();

  public void add(String key, String value) {
    assert key.contains(" ") == false;
    if (value != null) {
      inner.set(key, value);
    } else {
      remove(key);
    }
  }

  public IMap<String, String> getAll2(){
    eng.eSystem.collections.IMap<String, String> ret = new EMap<>();
    for (Map.Entry<String, String> stringStringEntry : inner) {
      ret.set(stringStringEntry.getKey(), stringStringEntry.getValue());
    }
    return ret;
  }

  public void setAll2(IMap<String, String> shortcuts){
    for (Map.Entry<String, String> shortcut : shortcuts) {
      inner.set(shortcut.getKey(), shortcut.getValue());
    }
  }

  public void remove(String key){
    if (inner.containsKey(key)) inner.remove(key);
  }

  public String tryGet(String key){
    String ret = inner.tryGet(key);
    return ret;
  }

  public ISet<Map.Entry<String, String>> getEntries(){
    ISet<Map.Entry<String, String>> ret = inner.getEntries();
    return ret;
  }
}
