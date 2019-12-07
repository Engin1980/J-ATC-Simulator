package eng.jAtcSim.newLib.speaking.fromAtc.atc2atc;

import eng.jAtcSim.newLib.speaking.fromAtc.IAtc2Atc;
import eng.jAtcSim.newLib.world.ActiveRunway;

public class RunwayCheck implements IAtc2Atc {

  public enum eType {
    askForTime,
    doCheck
  }

  public ActiveRunway runway;
  public eType type;

  public RunwayCheck(ActiveRunway runway, eType type) {
    this.runway = runway;
    this.type = type;
  }

  @Override
  public boolean isRejection(){
    return false;
  }

  @Override
  public String toString() {
    StringBuilder ret = new StringBuilder();
    ret.append("Runway check - ");
    switch (type) {
      case askForTime:
        ret.append("question for time");
        break;
      case doCheck:
        ret.append("advice to do check");
        break;
      default:
        throw new UnsupportedOperationException();
    }
    if (runway != null)
      ret.append(" for runway" + runway.getName());
    else
      ret.append(" for all runways");
    return ret.toString();
  }
}
