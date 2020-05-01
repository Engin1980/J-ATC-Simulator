package eng.jAtcSim.newLib.atcs;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.atcs.planeResponsibility.AirplaneResponsibilityInfo;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.exceptions.ToDoException;

public class XAcc {
  public static Atc getAtc(String atcName) {
    throw new ToDoException();
  }

  public static IAirplane4Atc getPlane(Callsign callsign) {
    EAssert.Argument.isNotNull(callsign, "callsign");
    throw new ToDoException();
  }

  public static Atc getAtc(AtcId atcId) {
    EAssert.Argument.isNotNull(atcId, "atcId");
    throw new ToDoException();
  }

  public static IAirplane4Atc getPlane(AirplaneResponsibilityInfo airplaneResponsibilityInfo) {
    EAssert.Argument.isNotNull(airplaneResponsibilityInfo, "airplaneResponsibilityInfo");
    return XAcc.getPlane(airplaneResponsibilityInfo.getPlane());
  }

  public static Atc getAtc(AirplaneResponsibilityInfo airplaneResponsibilityInfo) {
    EAssert.Argument.isNotNull(airplaneResponsibilityInfo, "airplaneResponsibilityInfo");
    return XAcc.getAtc(airplaneResponsibilityInfo.getAtc());
  }
}
