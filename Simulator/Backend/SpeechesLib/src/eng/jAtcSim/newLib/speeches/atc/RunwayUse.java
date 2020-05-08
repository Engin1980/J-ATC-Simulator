package eng.jAtcSim.newLib.speeches.atc;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class RunwayUse implements IAtcSpeech {

  public enum eType {
    informationRequest,
    changeNowRequest
  }

  public final eType type;

  public RunwayUse(eType type) {
    this.type = type;
  }

  public eType getType() {
    return type;
  }

  @Override
  public String toString() {
    return sf("Runway use{%s}", type.toString());
  }
}
