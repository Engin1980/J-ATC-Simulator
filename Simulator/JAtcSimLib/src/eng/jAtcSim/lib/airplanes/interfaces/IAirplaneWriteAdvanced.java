package eng.jAtcSim.lib.airplanes.interfaces;

import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.lib.airplanes.moods.Mood;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.GoingAroundNotification;
import eng.jAtcSim.lib.world.ActiveRunwayThreshold;
import eng.jAtcSim.lib.world.Navaid;
import eng.jAtcSim.lib.world.Route;
import eng.jAtcSim.lib.world.newApproaches.NewApproachInfo;

public interface IAirplaneWriteAdvanced {
  void abortHolding();

  void addExperience(Mood.ArrivalExperience experience);

  void addExperience(Mood.DepartureExperience experience);

  void clearedToApproach(NewApproachInfo newApproachInfo);

  void divert(boolean isInvokedByAtc);

  void goAround(GoingAroundNotification.GoAroundReason reason);

  void hold(Navaid navaid, int inboundRadial, boolean leftTurn);

  void setHoldingPointState(Coordinate coordinate, int course);

  @Deprecated
  void setRoute(SpeechList route);

  void setRouting(Route route, ActiveRunwayThreshold activeRunwayThreshold);

  void takeOff(ActiveRunwayThreshold runwayThreshold);
}
