package eng.jAtcSim.newLib.shared.enums;

import eng.eSystem.collections.*;

public enum DARouteType {
    sid,
    star,
    transition,
    vectoring;

    public boolean isArrival() {
      return this == star || this == transition;
    }
}
