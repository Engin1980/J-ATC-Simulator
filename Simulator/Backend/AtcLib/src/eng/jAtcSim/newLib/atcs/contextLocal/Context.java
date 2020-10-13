package eng.jAtcSim.newLib.atcs.contextLocal;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.IAirplane;
import eng.jAtcSim.newLib.airplanes.context.IAirplaneAcc;
import eng.jAtcSim.newLib.area.ActiveRunway;
import eng.jAtcSim.newLib.area.context.IAreaAcc;
import eng.jAtcSim.newLib.atcs.AtcList;
import eng.jAtcSim.newLib.atcs.context.IAtcAcc;
import eng.jAtcSim.newLib.atcs.internal.Atc;
import eng.jAtcSim.newLib.messaging.context.IMessagingAcc;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.ContextManager;
import eng.jAtcSim.newLib.shared.Squawk;
import eng.jAtcSim.newLib.shared.context.IAppAcc;
import eng.jAtcSim.newLib.shared.context.ISharedAcc;
import eng.jAtcSim.newLib.shared.enums.AtcType;
import eng.jAtcSim.newLib.stats.context.IStatsAcc;
import eng.jAtcSim.newLib.weather.context.IWeatherAcc;

public class Context {

  public static class Internal {
    private static Atc app;
    private static AtcList<Atc> atcs;
    //TODO del if unused
//    private static PlaneResponsibilityEvidence pre;

    public static Atc getApp() {
      EAssert.isNotNull(app);
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
      Atc ret = atcs.getFirst(q -> q.getAtcId().equals(atcId));
      return ret;
    }

    public static AtcList<Atc> getAtcs() {
      return atcs;
    }

    //TODO delete when unused
//    public static Atc getAtc(AirplaneResponsibilityInfo airplaneResponsibilityInfo) {
//      EAssert.Argument.isNotNull(airplaneResponsibilityInfo, "airplaneResponsibilityInfo");
//      return Context.Internal.getAtc(airplaneResponsibilityInfo.getAtc());
//    }

    public static IAirplane getPlane(Callsign callsign) {
      EAssert.Argument.isNotNull(callsign, "callsign");
      return Context.getAirplane().getAirplanes().get(callsign);
    }

    //TODO delete when unused
//    public static IAirplane getPlane(AirplaneResponsibilityInfo airplaneResponsibilityInfo) {
//      EAssert.Argument.isNotNull(airplaneResponsibilityInfo, "airplaneResponsibilityInfo");
//      return Context.Internal.getPlane(airplaneResponsibilityInfo.getPlane());
//    }

    public static IAirplane getPlane(Squawk squawk) {
      EAssert.Argument.isNotNull(squawk, "squawk");
      return Context.getAirplane().getAirplanes().get(squawk);
    }
    //TODO del if unused
//    public static PlaneResponsibilityEvidence getPre() {
//      return pre;
//    }

    public static ActiveRunway getRunway(String rwyName) {
      return Context.getArea().getAirport().getRunways().getFirst(q -> q.getName().equals(rwyName));
    }

    public static void init(AtcList<Atc> atcs, Atc app) {
      EAssert.Argument.isNotNull(atcs, "atcs");
      EAssert.Argument.isNotNull(app, "app");
      Context.Internal.app = app;
      Context.Internal.atcs = atcs;
    }
  }

  public static IAirplaneAcc getAirplane() {
    return ContextManager.getContext(IAirplaneAcc.class);
  }

  public static IAppAcc getApp() {
    return ContextManager.getContext(IAppAcc.class);
  }

  public static IAreaAcc getArea() {
    return ContextManager.getContext(IAreaAcc.class);
  }

  public static IAtcAcc getAtc() {
    return ContextManager.getContext(IAtcAcc.class);
  }

  public static IMessagingAcc getMessaging() {
    return ContextManager.getContext(IMessagingAcc.class);
  }

  public static ISharedAcc getShared() {
    return ContextManager.getContext(ISharedAcc.class);
  }

  public static IStatsAcc getStats() {
    return ContextManager.getContext(IStatsAcc.class);
  }

  public static IWeatherAcc getWeather() {
    return ContextManager.getContext(IWeatherAcc.class);
  }
}
