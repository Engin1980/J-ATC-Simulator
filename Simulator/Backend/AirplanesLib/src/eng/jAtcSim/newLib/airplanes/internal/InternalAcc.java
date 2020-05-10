package eng.jAtcSim.newLib.airplanes.internal;

import eng.eSystem.Producer;
import eng.jAtcSim.newLib.airplanes.AirplaneList;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.area.AreaAcc;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.area.PublishedHold;
import eng.jAtcSim.newLib.area.routes.DARoute;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ToNavaidCommand;

public class InternalAcc {
  private static Producer<AirplaneList<Airplane>> airplanesProducer = null;

  public static AirplaneList<Airplane> getAirplanes() {
    return airplanesProducer.produce();
  }

  public static Navaid getNavaid(ToNavaidCommand toNavaidCommand) {
    return AreaAcc.getNavaids().get(toNavaidCommand.getNavaidName());
  }

  public static void setAirplaneListProducer(Producer<AirplaneList<Airplane>> airplanesProducer) {
    InternalAcc.airplanesProducer = airplanesProducer;
  }

  public static DARoute tryGetDARoute(String routeName) {
    return AreaAcc.getAirport().getDaRoutes().getFirst(q -> q.getName().equals(routeName));
  }

  public static Navaid tryGetNavaid(String navaidName) {
    return AreaAcc.getNavaids().get(navaidName);
  }

  public static PublishedHold tryGetPublishedHold(String navaidName) {
    return AreaAcc.getAirport().getHolds().getFirst(q -> q.getNavaid().equals(navaidName));
  }

  public static ActiveRunwayThreshold tryGetRunwayThreshold(String runwayThresholdName) {
    return AreaAcc.getAirport().getAllThresholds().getFirst(q -> q.getName().equals(runwayThresholdName));
  }
}
