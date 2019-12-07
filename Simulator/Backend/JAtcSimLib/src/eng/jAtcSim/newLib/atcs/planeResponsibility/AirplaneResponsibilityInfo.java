package eng.jAtcSim.newLib.atcs.planeResponsibility;

import eng.eSystem.validation.Validator;
;
import eng.eSystem.xmlSerialization.annotations.XmlConstructor;
import eng.jAtcSim.newLib.airplanes.Airplane;
import eng.jAtcSim.newLib.atcs.Atc;

public class AirplaneResponsibilityInfo {
  private Airplane plane;
  private Atc atc;
  private SwitchRequest switchRequest;

  @XmlConstructor
  private AirplaneResponsibilityInfo(){}

  public AirplaneResponsibilityInfo(Airplane plane, Atc atc) {
    Validator.isNotNull(plane);
    Validator.isNotNull(atc);

    this.plane = plane;
    this.atc = atc;
    this.switchRequest = null;
  }

  public Airplane getPlane() {
    return plane;
  }

  public Atc getAtc() {
    return atc;
  }

  void setAtc(Atc atc) {
    Validator.isNotNull(atc);
    this.atc = atc;
  }

  public SwitchRequest getSwitchRequest() {
    return switchRequest;
  }

  void setSwitchRequest(SwitchRequest switchRequest) {
    this.switchRequest = switchRequest;
  }
}
