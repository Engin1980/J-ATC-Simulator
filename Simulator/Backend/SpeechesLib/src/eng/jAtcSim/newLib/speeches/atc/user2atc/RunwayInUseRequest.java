package eng.jAtcSim.newLib.speeches.atc.user2atc;

import eng.jAtcSim.newLib.speeches.atc.IAtcSpeech;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class RunwayInUseRequest implements IAtcSpeech {

  public enum eType {
    informationRequest,
    changeNowRequest
  }

  public final eType type;

  public RunwayInUseRequest(eType type) {
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
