package eng.jAtcSim.lib.airplanes.navigators;

import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.lib.airplanes.modules.ShaModule;
import eng.jAtcSim.lib.global.HeadingsNew;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ChangeHeadingCommand;

public class HeadingNavigator implements INavigator {
  public enum Turn {
    any,
    left,
    right
  }

  private static int doubleHeadingToInt(double heading) {
    return (int) Math.round((heading));
  }

  private final Turn turn;
  private final int heading;
  private boolean isApplied;

  public HeadingNavigator(int heading, Turn turn) {
    this.heading = heading;
    this.turn = turn;
    this.isApplied = false;
  }

  public HeadingNavigator(int heading) {
    this(heading, Turn.any);
  }

  public HeadingNavigator(double heading) {
    this(doubleHeadingToInt(heading));
  }

  public HeadingNavigator(double heading, Turn turn) {
    this(doubleHeadingToInt(heading), turn);
  }

  @Override
  public void navigate(ShaModule sha, Coordinate planeCoordinates) {
    if (!this.isApplied) {
      boolean useLeftTurn;
      switch(turn){
        case any:
          useLeftTurn
              = HeadingsNew.getBetterDirectionToTurn(sha.getHeading(), this.heading) == ChangeHeadingCommand.eDirection.left;
          break;
        case right:
          useLeftTurn = false;
          break;
        case left:
          useLeftTurn = true;
          break;
        default:
          throw new EEnumValueUnsupportedException(turn);
      }
      sha._setTargetHeading(heading, useLeftTurn);
      this.isApplied = true;
    }
  }
}
