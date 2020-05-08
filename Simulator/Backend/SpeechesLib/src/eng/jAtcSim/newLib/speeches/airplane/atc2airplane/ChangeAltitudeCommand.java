package eng.jAtcSim.newLib.speeches.airplane.atc2airplane;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.speeches.airplane.ICommand;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class ChangeAltitudeCommand implements ICommand {

  public enum eDirection {
    any,
    climb,
    descend
  }

  public static ChangeAltitudeCommand create(eDirection direction, int altitudeInFt) {
    ChangeAltitudeCommand ret = new ChangeAltitudeCommand(direction, altitudeInFt);
    return ret;
  }

  public static ChangeAltitudeCommand create(int altitudeInFt) {
    ChangeAltitudeCommand ret = new ChangeAltitudeCommand(eDirection.any, altitudeInFt);
    return ret;
  }

  public static ChangeAltitudeCommand createClimb(int altitudeInFt) {
    ChangeAltitudeCommand ret = new ChangeAltitudeCommand(eDirection.climb, altitudeInFt);
    return ret;
  }

  public static ChangeAltitudeCommand createDescend(int altitudeInFt) {
    ChangeAltitudeCommand ret = new ChangeAltitudeCommand(eDirection.descend, altitudeInFt);
    return ret;
  }

  private final eDirection direction;
  private final int altitudeInFt;

  private ChangeAltitudeCommand(eDirection direction, int altitudeInFt) {
    EAssert.Argument.isTrue(altitudeInFt >= 0,
        sf("Altitude must be greater or equal to zero (current: %d).", altitudeInFt));
    this.direction = direction;
    this.altitudeInFt = altitudeInFt;
  }

  public int getAltitudeInFt() {
    return altitudeInFt;
  }

  public eDirection getDirection() {
    return direction;
  }

  @Override
  public String toString() {
    return "Altitude change to " + altitudeInFt + " {command}";
  }
}
