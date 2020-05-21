package eng.jAtcSim.newLib.shared.enums;

import eng.eSystem.collections.*;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;

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

  @Override
  public String toString(){
    switch (this){
      case gnss:
        return "GNSS";
      case ils_I:
        return "ILS I";
      case ils_II:
        return "ILS II";
      case ils_III:
        return "ILS III";
      case ndb:
        return "NDB";
      case visual:
        return "visual";
      case vor:
        return "VOR";
      default:
        throw new EEnumValueUnsupportedException(this);
    }
  }
}
