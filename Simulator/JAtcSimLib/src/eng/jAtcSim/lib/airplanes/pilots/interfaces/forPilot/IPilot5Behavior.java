package eng.jAtcSim.lib.airplanes.pilots.interfaces.forPilot;

import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.moods.Mood;
import eng.jAtcSim.lib.airplanes.pilots.Pilot;
import eng.jAtcSim.lib.airplanes.pilots.behaviors.Behavior;
import eng.jAtcSim.lib.airplanes.pilots.interfaces.forAirplane.IAirplaneRO;
import eng.jAtcSim.lib.airplanes.pilots.navigators.INavigator;
import eng.jAtcSim.lib.speaking.ISpeech;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.GoingAroundNotification;
import eng.jAtcSim.lib.world.Navaid;

public interface IPilot5Behavior {

  IAirplaneRO getPlane();

  void setBehaviorAndState(Behavior behavior, Airplane.State state);

  IDivertModuleRO getDivertModule();

  void say(ISpeech speech);

  void processDivert();

  void setTargetHeading(double targetHeading);

//TODO delete this?
  void setHoldBehavior(Navaid navaid, int inboundRadial, boolean leftTurn);

  void setTargetCoordinate(Coordinate coordinate);

  void setTargetHeading(double heading, boolean isLeftTurned);

  void experience(Mood.ArrivalExperience experience);

  void experience(Mood.DepartureExperience experience);

  void adjustTargetSpeed();

  void setState(Airplane.State state);

  void setRoute(SpeechList route);

  boolean hasEmptyRoute();

  void setTargetAltitude(double altitude);

  void goAround(GoingAroundNotification.GoAroundReason reason, double course, SpeechList gaRoute);

  void setNavigator(INavigator navigator);

  IRoutingModuleRO getRoutingModule();

  void setLastAnnouncedMinuteForDivert(int minLeft);
}
