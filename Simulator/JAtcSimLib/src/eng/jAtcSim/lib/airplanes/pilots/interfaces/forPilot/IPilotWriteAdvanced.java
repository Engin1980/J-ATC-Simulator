package eng.jAtcSim.lib.airplanes.pilots.interfaces.forPilot;

import eng.jAtcSim.lib.airplanes.moods.Mood;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.GoingAroundNotification;
import eng.jAtcSim.lib.world.ActiveRunwayThreshold;
import eng.jAtcSim.lib.world.Navaid;

public interface IPilotWriteAdvanced {
  void abortHolding();

  void addExperience(Mood.ArrivalExperience experience);

  void addExperience(Mood.DepartureExperience experience);

  void goAround(GoingAroundNotification.GoAroundReason reason);

  void hold(Navaid navaid, int inboundRadial, boolean leftTurn);

  @Deprecated
  void setRoute(SpeechList route);
  @Deprecated
  void setRoute(SpeechList route, ActiveRunwayThreshold activeRunwayThreshold);

  void divert();

  void takeOff(ActiveRunwayThreshold runwayThreshold);
}
