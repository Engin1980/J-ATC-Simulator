package eng.jAtcSim.newLib.shared;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;

public class NavaidMap {
  private final IMap<String, Navaid> inner = new EMap<>();

  public void add(Navaid navaid) {
    this.inner.set(navaid.getName(), navaid);
  }

  public Navaid get(Navaid navaid) {
    return inner.get(navaid.getName());
  }

  public Navaid tryGet(Navaid navaid) {
    return inner.tryGet(navaid.getName());
  }
}
