package eng.jAtcSim.newLib.speeches.atc2atc;

import eng.jAtcSim.newLib.speeches.ICommand;
import eng.jAtcSim.newLib.speeches.IRejectable;

public class RunwayCheck implements ICommand, IRejectable {

  public enum eType {
    askForTime,
    doCheck
  }

  public String runway;
  public eType type;

  public RunwayCheck(String runway, eType type) {
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
      ret.append(" for runway").append(runway);
    else
      ret.append(" for all runways");
    return ret.toString();
  }
}
