package eng.jAtcSim.newLib.speeches.atc2airplane;

import eng.jAtcSim.newLib.speeches.IAtcCommand;

public class ChangeAltitudeCommand implements IAtcCommand {

  public enum eDirection{
    any,
    climb,
    descend
  }
  
  private final eDirection direction;
  private final int altitudeInFt;

  public static ChangeAltitudeCommand create(eDirection direction, int altitudeInFt){
    ChangeAltitudeCommand ret = new ChangeAltitudeCommand(direction, altitudeInFt);
    return ret;
  }

  public static ChangeAltitudeCommand createClimb(int altitudeInFt){
    ChangeAltitudeCommand ret = new ChangeAltitudeCommand(eDirection.climb, altitudeInFt);
    return ret;
  }

  public static ChangeAltitudeCommand createDescend(int altitudeInFt){
    ChangeAltitudeCommand ret = new ChangeAltitudeCommand(eDirection.descend, altitudeInFt);
    return ret;
  }

  public static ChangeAltitudeCommand create(int altitudeInFt){
    ChangeAltitudeCommand ret = new ChangeAltitudeCommand(eDirection.any, altitudeInFt);
    return ret;
  }

  private ChangeAltitudeCommand(eDirection direction, int altitudeInFt) {
    this.direction = direction;
    this.altitudeInFt = altitudeInFt;
  }

  public eDirection getDirection() {
    return direction;
  }

  public int getAltitudeInFt() {
    return altitudeInFt;
  }
  
    @Override
  public String toString() {
    return "Altitude change to "+ altitudeInFt + " {command}";
  }
}
