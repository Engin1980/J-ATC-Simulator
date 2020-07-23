package eng.jAtcSim.newLib.airplanes.internal;

import eng.eSystem.functionalInterfaces.Producer;
import eng.jAtcSim.newLib.airplanes.AirplaneList;
import eng.jAtcSim.newLib.airplanes.contextLocal.Context;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.area.context.AreaAcc;
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
    return Context.getArea().getNavaids().get(toNavaidCommand.getNavaidName());
  }

  public static void setAirplaneListProducer(Producer<AirplaneList<Airplane>> airplanesProducer) {
    InternalAcc.airplanesProducer = airplanesProducer;
  }

  public static DARoute tryGetDARoute(String routeName) {
    return Context.getArea().getAirport().getDaRoutes().getFirst(q -> q.getName().equals(routeName));
  }

  public static Navaid tryGetNavaid(String navaidName) {
    return Context.getArea().getNavaids().get(navaidName);
  }

  public static PublishedHold tryGetPublishedHold(String navaidName) {
    return Context.getArea().getAirport().getHolds().getFirst(q -> q.getNavaid().equals(navaidName));
  }

  public static ActiveRunwayThreshold tryGetRunwayThreshold(String runwayThresholdName) {
    return Context.getArea().getAirport().getAllThresholds().getFirst(q -> q.getName().equals(runwayThresholdName));
  }
}
