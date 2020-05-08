package eng.jAtcSim.newLib.shared.enums;

import eng.eSystem.collections.*;

public enum ApproachType {
  ils_I,
  ils_II,
  ils_III,
  ndb,
  vor,
  gnss,
  visual;

  public boolean isILS() {
    return this == ils_I || this == ils_II || this == ils_III;
  }

  public boolean isUnprecise() {
    return this == vor || this == ndb;
  }
}
