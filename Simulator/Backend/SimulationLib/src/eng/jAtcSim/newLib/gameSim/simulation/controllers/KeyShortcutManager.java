package eng.jAtcSim.newLib.gameSim.simulation.controllers;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.eSystem.collections.IReadOnlyMap;

public class KeyShortcutManager {

  private IMap<String, String> inner = new EMap<>();

  public void shortcutDeletion(String key) {
    this.inner.tryRemove(key);
  }

  public IReadOnlyMap<String, String> shortcutList() {
    return this.inner;

  }

  public void shortcutSet(String key, String value) {
    this.inner.set(key, value);
  }
}
