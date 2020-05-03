package eng.jAtcSim.newLib.atcs.internal;

import eng.eSystem.Producer;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.AirplaneAcc;
import eng.jAtcSim.newLib.airplanes.IAirplane;
import eng.jAtcSim.newLib.area.ActiveRunway;
import eng.jAtcSim.newLib.area.AreaAcc;
import eng.jAtcSim.newLib.atcs.AtcList;
import eng.jAtcSim.newLib.atcs.planeResponsibility.AirplaneResponsibilityInfo;
import eng.jAtcSim.newLib.atcs.planeResponsibility.PlaneResponsibilityManager;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.enums.AtcType;
import eng.jAtcSim.newLib.shared.exceptions.ToDoException;

public class InternalAcc {
  private static final PlaneResponsibilityManager prm = new PlaneResponsibilityManager();

  private static Producer<AtcList<Atc>> atcProducer = null;
  private static Atc twr;
  private static Atc app;
  private static Atc ctr;

  public static Atc getApp() {
    if (app == null)
      app = atcProducer.produce().getFirst(q -> q.getAtcId().getType() == AtcType.app);
    return app;
  }

  public static AtcList<Atc> getAtcs(){
    return atcProducer.produce();
  }

  public static Atc getAtc(String atcName) {
    return getAtcs().getFirst(q->q.getAtcId().getName().equals(atcName));
  }

  public static Atc getAtc(AtcType atcType) {
    return getAtcs().getFirst(q->q.getAtcId().getType() == atcType);
  }

  public static Atc getAtc(AtcId atcId) {
    EAssert.Argument.isNotNull(atcId, "atcId");
    throw new ToDoException();
  }

  public static Atc getAtc(AirplaneResponsibilityInfo airplaneResponsibilityInfo) {
    EAssert.Argument.isNotNull(airplaneResponsibilityInfo, "airplaneResponsibilityInfo");
    return InternalAcc.getAtc(airplaneResponsibilityInfo.getAtc());
  }

  public static Atc getCtr() {
    if (ctr == null)
      ctr = atcProducer.produce().getFirst(q -> q.getAtcId().getType() == AtcType.ctr);
    return ctr;
  }

  public static IAirplane getPlane(AirplaneResponsibilityInfo airplaneResponsibilityInfo) {
    EAssert.Argument.isNotNull(airplaneResponsibilityInfo, "airplaneResponsibilityInfo");
    return InternalAcc.getPlane(airplaneResponsibilityInfo.getPlane());
  }

  public static IAirplane getPlane(Callsign callsign) {
    EAssert.Argument.isNotNull(callsign, "callsign");
    return AirplaneAcc.getAirplanes().get(callsign);
  }

  public static PlaneResponsibilityManager getPrm() {
    return prm;
  }

  public static ActiveRunway getRunway(String rwyName) {
    return AreaAcc.getAirport().getRunways().getFirst(q->q.getName().equals(rwyName));
  }

  public static Atc getTwr() {
    if (twr == null)
      twr = atcProducer.produce().getFirst(q -> q.getAtcId().getType() == AtcType.twr);
    return twr;
  }

  public static void setAtcProducer(Producer<AtcList<Atc>> atcProducer) {
    InternalAcc.atcProducer = atcProducer;
    twr = null;
    app = null;
    ctr = null;
  }
}
