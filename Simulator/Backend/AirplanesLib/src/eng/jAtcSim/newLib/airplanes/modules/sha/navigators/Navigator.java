package eng.jAtcSim.newLib.airplanes.modules.sha.navigators;

import eng.eSystem.geo.Headings;
import eng.jAtcSim.newLib.airplanes.IAirplane;
import eng.jAtcSim.newLib.shared.enums.LeftRight;
import exml.IXPersistable;

public abstract class Navigator implements IXPersistable {
  public abstract NavigatorResult navigate(IAirplane plane);

  public static LeftRight getBetterDirectionToTurn(double current, double target) {
    target = target - current;
    target = Headings.to(target);
    if (target > 180)
      return LeftRight.left;
    else
      return LeftRight.right;
  }
}
