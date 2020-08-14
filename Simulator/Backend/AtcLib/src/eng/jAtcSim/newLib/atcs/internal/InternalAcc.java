package eng.jAtcSim.newLib.atcs.internal;

import eng.eSystem.collections.IList;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.ToDoException;
import eng.eSystem.functionalInterfaces.Producer;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.IAirplane;
import eng.jAtcSim.newLib.area.ActiveRunway;
import eng.jAtcSim.newLib.atcs.AtcList;
import eng.jAtcSim.newLib.atcs.contextLocal.Context;
import eng.jAtcSim.newLib.atcs.planeResponsibility.AirplaneResponsibilityInfo;
import eng.jAtcSim.newLib.atcs.planeResponsibility.PlaneResponsibilityManager;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.Squawk;
import eng.jAtcSim.newLib.shared.context.IAppAcc;
import eng.jAtcSim.newLib.shared.enums.AtcType;

public class InternalAcc {
  private static Atc app;
  private static AtcList<Atc> atcs;
  private static final PlaneResponsibilityManager prm = new PlaneResponsibilityManager();

  public static void init(AtcList<Atc> atcs, Atc app){
    EAssert.Argument.isNotNull(atcs, "atcs");
    EAssert.Argument.isNotNull(app, "app");
    InternalAcc.app = app;
    InternalAcc.atcs = atcs;
  }

  public static Atc getApp() {
    EAssert.isNotNull(app );
    return app;
  }

  public static Atc getAtc(String atcName) {
    return getAtcs().getFirst(q -> q.getAtcId().getName().equals(atcName));
  }

  public static Atc getAtc(AtcType atcType) {
    return getAtcs().getFirst(q -> q.getAtcId().getType() == atcType);
  }

  public static Atc getAtc(AtcId atcId) {
    EAssert.Argument.isNotNull(atcId, "atcId");
    EAssert.isNotNull(atcs);
    Atc ret = atcs.getFirst(q->q.getAtcId().equals(atcId));
    return ret;
  }

  public static Atc getAtc(AirplaneResponsibilityInfo airplaneResponsibilityInfo) {
    EAssert.Argument.isNotNull(airplaneResponsibilityInfo, "airplaneResponsibilityInfo");
    return InternalAcc.getAtc(airplaneResponsibilityInfo.getAtc());
  }

  public static AtcList<Atc> getAtcs() {
    return atcs;
  }

  public static Callsign getCallsignFromSquawk(Squawk squawk) {
    return Context.getAirplane().getAirplanes().get(squawk).getCallsign();
  }

  public static IAirplane getPlane(AirplaneResponsibilityInfo airplaneResponsibilityInfo) {
    EAssert.Argument.isNotNull(airplaneResponsibilityInfo, "airplaneResponsibilityInfo");
    return InternalAcc.getPlane(airplaneResponsibilityInfo.getPlane());
  }

  public static IAirplane getPlane(Callsign callsign) {
    EAssert.Argument.isNotNull(callsign, "callsign");
    return Context.getAirplane().getAirplanes().get(callsign);
  }

  public static PlaneResponsibilityManager getPrm() {
    return prm;
  }

  public static ActiveRunway getRunway(String rwyName) {
    return Context.getArea().getAirport().getRunways().getFirst(q -> q.getName().equals(rwyName));
  }

  public static Squawk getSquawkFromCallsign(Callsign callsign) {
    return Context.getAirplane().getAirplanes().get(callsign).getSqwk();
  }
}
