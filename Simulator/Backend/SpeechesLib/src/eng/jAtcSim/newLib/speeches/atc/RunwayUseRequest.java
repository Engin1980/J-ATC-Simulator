package eng.jAtcSim.newLib.speeches.atc;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class RunwayUseRequest implements IAtcSpeech {

  public enum eType {
    informationRequest,
    changeNowRequest
  }

  public final eType type;

  public RunwayUseRequest(eType type) {
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
