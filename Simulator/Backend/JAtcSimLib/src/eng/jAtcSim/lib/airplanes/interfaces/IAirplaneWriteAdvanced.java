package eng.jAtcSim.lib.airplanes.interfaces;

import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.lib.airplanes.moods.Mood;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.GoingAroundNotification;
import eng.jAtcSim.lib.world.ActiveRunwayThreshold;
import eng.jAtcSim.lib.world.Navaid;
import eng.jAtcSim.lib.world.DARoute;
import eng.jAtcSim.lib.world.approaches.NewApproachInfo;

public interface IAirplaneWriteAdvanced {
  void abortHolding();

  void addExperience(Mood.ArrivalExperience experience);

  void addExperience(Mood.DepartureExperience experience);

  void addExperience(Mood.SharedExperience experience);

  void clearedToApproach(NewApproachInfo newApproachInfo);

  void divert(boolean isInvokedByAtc);

  void goAround(GoingAroundNotification.GoAroundReason reason);

  void hold(Navaid navaid, int inboundRadial, boolean leftTurn);

  void raiseEmergency();

  void setHoldingPointState(Coordinate coordinate, int course);

  @Deprecated
  void setRoute(SpeechList route);

  void setRouting(DARoute route, ActiveRunwayThreshold activeRunwayThreshold);

  void takeOff(ActiveRunwayThreshold runwayThreshold);
}
