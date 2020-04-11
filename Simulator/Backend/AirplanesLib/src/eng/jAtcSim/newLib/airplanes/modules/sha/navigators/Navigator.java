package eng.jAtcSim.newLib.airplanes.modules.sha.navigators;

import eng.eSystem.geo.Headings;
import eng.jAtcSim.newLib.airplanes.modules.IModulePlane;
import eng.jAtcSim.newLib.shared.enums.LeftRight;

public abstract class Navigator {
  public abstract NavigatorResult navigate(IModulePlane plane);

  public static LeftRight getBetterDirectionToTurn(double current, double target) {
    target = target - current;
    target = Headings.to(target);
    if (target > 180)
      return LeftRight.left;
    else
      return LeftRight.right;
  }
}
