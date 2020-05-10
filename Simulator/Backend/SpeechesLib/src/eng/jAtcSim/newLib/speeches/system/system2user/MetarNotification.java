package eng.jAtcSim.newLib.speeches.system.system2user;

import eng.eSystem.collections.*;
import eng.jAtcSim.newLib.speeches.system.ISystemNotification;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class MetarNotification implements ISystemNotification {
  private final boolean updated;

  public boolean isUpdated() {
    return updated;
  }

  public MetarNotification(boolean updated) {
    this.updated = updated;
  }
}
