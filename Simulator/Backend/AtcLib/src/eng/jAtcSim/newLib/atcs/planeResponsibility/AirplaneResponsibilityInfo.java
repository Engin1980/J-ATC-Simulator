package eng.jAtcSim.newLib.atcs.planeResponsibility;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Callsign;

public class AirplaneResponsibilityInfo {
  private final Callsign plane;
  private AtcId atc;
  private SwitchRequest switchRequest;

  public AirplaneResponsibilityInfo(Callsign plane, AtcId atc) {
    EAssert.Argument.isNotNull(plane, "plane");
    EAssert.Argument.isNotNull(atc, "atc");

    this.plane = plane;
    this.atc = atc;
    this.switchRequest = null;
  }

  public Callsign getPlane() {
    return plane;
  }

  public AtcId getAtc() {
    return atc;
  }

  void setAtc(AtcId atc) {
    //TODO why? why not only in constructor?
    EAssert.Argument.isNotNull(atc, "atc");
    this.atc = atc;
  }

  public SwitchRequest getSwitchRequest() {
    return switchRequest;
  }

  void setSwitchRequest(SwitchRequest switchRequest) {
    this.switchRequest = switchRequest;
  }
}
