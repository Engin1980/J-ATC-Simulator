package eng.jAtcSim.newLib.atcs.internal;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.IAirplane;
import eng.jAtcSim.newLib.atcs.planeResponsibility.AirplaneResponsibilityInfo;
import eng.jAtcSim.newLib.atcs.planeResponsibility.PlaneResponsibilityManager;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.enums.AtcType;
import eng.jAtcSim.newLib.shared.exceptions.ToDoException;

public class XAcc {
  private static final PlaneResponsibilityManager prm = new PlaneResponsibilityManager();

  public static Atc getAtc(String atcName) {
    throw new ToDoException();
  }

  public static Atc getAtc(AtcType atcType) {
    throw new ToDoException();
  }

  public static Atc getAtc(AtcId atcId) {
    EAssert.Argument.isNotNull(atcId, "atcId");
    throw new ToDoException();
  }

  public static Atc getAtc(AirplaneResponsibilityInfo airplaneResponsibilityInfo) {
    EAssert.Argument.isNotNull(airplaneResponsibilityInfo, "airplaneResponsibilityInfo");
    return XAcc.getAtc(airplaneResponsibilityInfo.getAtc());
  }

  public static IAirplane getPlane(AirplaneResponsibilityInfo airplaneResponsibilityInfo) {
    EAssert.Argument.isNotNull(airplaneResponsibilityInfo, "airplaneResponsibilityInfo");
    return XAcc.getPlane(airplaneResponsibilityInfo.getPlane());
  }

  public static IAirplane getPlane(Callsign callsign) {
    EAssert.Argument.isNotNull(callsign, "callsign");
    throw new ToDoException();
  }

  public static PlaneResponsibilityManager getPrm() {
    return prm;
  }
}
