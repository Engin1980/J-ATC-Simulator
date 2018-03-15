package eng.jAtcSim.lib.speaking.parsing;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ShortcutList {

  public HashMap m;
  private Map<String, String> inner = new HashMap<>();

  public void add(String key, String value) {
    assert key.contains(" ") == false;
    if (value != null) {
      inner.put(key, value);
    } else {
      remove(key);
    }
  }

  public void remove(String key){
    if (inner.containsKey(key)) inner.remove(key);
  }

  public String tryGet(String key){
    String ret = inner.getOrDefault(key, null);
    return ret;
  }

  public Set<Map.Entry<String, String>> getAll(){
    Set<Map.Entry<String, String>> ret = inner.entrySet();
    return ret;
  }
}
