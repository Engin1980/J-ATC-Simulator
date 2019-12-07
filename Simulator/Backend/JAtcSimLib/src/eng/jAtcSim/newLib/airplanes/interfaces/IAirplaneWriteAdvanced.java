package eng.jAtcSim.newLib.airplanes.interfaces;

import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.airplanes.moods.Mood;
import eng.jAtcSim.newLib.speaking.SpeechList;
import eng.jAtcSim.newLib.speaking.fromAirplane.notifications.GoingAroundNotification;
import eng.jAtcSim.newLib.world.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.world.Navaid;
import eng.jAtcSim.newLib.world.DARoute;
import eng.jAtcSim.newLib.world.approaches.NewApproachInfo;

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
