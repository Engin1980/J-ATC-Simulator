package eng.jAtcSim.newLib.shared.enums;

import eng.eSystem.exceptions.EEnumValueUnsupportedException;

public enum ApproachType {
  ils_I,
  ils_II,
  ils_III,
  ndb,
  vor,
  gnss,
  visual;

  public static ApproachType parse(String str) {
    switch (str) {
      case "GNSS":
        return gnss;
      case "ILS I":
        return ils_I;
      case "ILS II":
        return ils_II;
      case "ILS III":
        return ils_III;
      case "NDB":
        return ndb;
      case "visual":
        return visual;
      case "VOR":
        return vor;
      default:
        throw new EEnumValueUnsupportedException(str);
    }
  }

  public boolean isILS() {
    return this == ils_I || this == ils_II || this == ils_III;
  }

  public boolean isUnprecise() {
    return this == vor || this == ndb;
  }

  @Override
  public String toString() {
    switch (this) {
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
