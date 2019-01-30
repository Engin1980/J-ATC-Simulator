package eng.jAtcSim.lib.airplanes.pilots.behaviors;

import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.AirplaneType;
import eng.jAtcSim.lib.airplanes.moods.Mood;
import eng.jAtcSim.lib.airplanes.pilots.Pilot;
import eng.jAtcSim.lib.speaking.INotification;
import eng.jAtcSim.lib.speaking.ISpeech;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.DivertTimeNotification;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.PassingClearanceLimitNotification;
import eng.jAtcSim.lib.world.Navaid;
import eng.jAtcSim.lib.world.Route;

public interface IPilot4Behavior {

  void setBehaviorAndState(Behavior behavior, Airplane.State state);

  Airplane.State getState();

  Pilot.DivertInfo getDivertInfo();

  void say(ISpeech speech);

  void processDivert();

  AirplaneType getAirplaneType();

  Coordinate getCoordinate();

  void setTargetHeading(double targetHeading);

  double getSpeed();

  double getAltitude();

  boolean isArrival();

  Coordinate getTargetCoordinate();

  boolean hasLateralDirectionAfterCoordinate();

  Route getAssignedRoute();


  void setHoldBehavior(Navaid navaid, int inboundRadial, boolean leftTurn);

  void setTargetCoordinate(Coordinate coordinate);

  double getTargetHeading();

  boolean isEmergency();

  void setTargetHeading(double heading, boolean isLeftTurned);

  void experience(Mood.ArrivalExperience experience);

  /**
   *
   * @param experience
   * Pilot.this.parent.getMood()
   */
  void experience(Mood.DepartureExperience experience);

  double getHeading();

  void adjustTargetSpeed();
}
