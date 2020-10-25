package eng.jAtcSim.newLib.airplanes.modules.sha.navigators;

import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.jAtcSim.newLib.airplanes.IAirplane;
import eng.jAtcSim.newLib.shared.enums.LeftRight;
import eng.jAtcSim.newLib.shared.enums.LeftRightAny;

public class HeadingNavigator extends Navigator {

  private static int doubleHeadingToInt(double heading) {
    return (int) Math.round((heading));
  }

  private final LeftRightAny turn;
  private final int heading;
  private boolean isApplied;

  public HeadingNavigator(int heading, LeftRightAny turn) {
    this.heading = heading;
    this.turn = turn;
    this.isApplied = false;
  }

  public HeadingNavigator(int heading) {
    this(heading, LeftRightAny.any);
  }

  public HeadingNavigator(double heading) {
    this(doubleHeadingToInt(heading));
  }

  public HeadingNavigator(double heading, LeftRightAny turn) {
    this(doubleHeadingToInt(heading), turn);
  }

  @Override
  public NavigatorResult navigate(IAirplane plane) {
    NavigatorResult ret;
    if (!this.isApplied) {
      LeftRight resultTurn;
      switch(turn){
        case any:
          resultTurn
              = getBetterDirectionToTurn(plane.getSha().getHeading(), this.heading);
          break;
        case right:
          resultTurn = LeftRight.right;
          break;
        case left:
          resultTurn = LeftRight.left;
          break;
        default:
          throw new EEnumValueUnsupportedException(turn);
      }
      this.isApplied = true;
      ret = new NavigatorResult(heading, resultTurn);
    } else
      ret = null;
    return ret;
  }

  @Override
  public String toString() {
    return "HeadingNavigator{" +
            "turn=" + turn +
            ", heading=" + heading +
            ", isApplied=" + isApplied +
            '}';
  }
}
