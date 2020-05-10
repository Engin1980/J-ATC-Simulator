package eng.jAtcSim.newLib.speeches.system.system2user;

import eng.eSystem.collections.*;
import eng.jAtcSim.newLib.speeches.system.ISystemNotification;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class CurrentTickNotification implements ISystemNotification {
  private final int tickInterval;
  private final boolean changed;

  public CurrentTickNotification(int tickInterval, boolean changed) {
    this.tickInterval = tickInterval;
    this.changed = changed;
  }

  public int getTickInterval() {
    return tickInterval;
  }

  public boolean isChanged() {
    return changed;
  }
}
