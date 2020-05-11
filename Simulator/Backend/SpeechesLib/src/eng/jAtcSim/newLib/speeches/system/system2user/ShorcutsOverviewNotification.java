package eng.jAtcSim.newLib.speeches.system.system2user;

import eng.eSystem.collections.*;
import eng.jAtcSim.newLib.speeches.system.ISystemNotification;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class ShorcutsOverviewNotification implements ISystemNotification {

  private final IReadOnlyMap<String,String> shortcuts;

  public ShorcutsOverviewNotification(IReadOnlyMap<String, String> shortcuts) {
    this.shortcuts = shortcuts;
  }

  public IReadOnlyMap<String, String> getShortcuts() {
    return shortcuts;
  }
}
