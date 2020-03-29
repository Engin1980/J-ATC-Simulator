package eng.jAtcSim.newLib.area.xml;

import eng.eSystem.collections.*;

public class XmlMappingDictinary<T> {
  private final IMap<String, IList<T>> inner = new EMap<>();

  public void add(String mapping, T ret) {
    String[] pts = mapping.toLowerCase().split(";");
    for (String pt : pts) {
      addInner(pt, ret);
    }
  }

  public IReadOnlyList<T> get(String mapping) {
    String[] pts = mapping.toLowerCase().split(";");
    IList<T> ret = new EList<>();
    for (String pt : pts) {
      IList<T> tmp = getInner(pt);
      ret.add(tmp);
    }
    return ret;
  }

  private void addInner(String mapping, T ret) {
    if (inner.containsKey(mapping) == false)
      inner.set(mapping, new EList<>());
    inner.get(mapping).add(ret);
  }

  private IList<T> getInner(String pt) {
    return this.inner.tryGet(pt, new EList<>());
  }
}
