package eng.jAtcSim.newLib.airplanes.pilots;

import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.airplanes.Airplane;
import eng.jAtcSim.newLib.mood.Mood;
import eng.jAtcSim.newLib.speeches.ISpeech;
import eng.jAtcSim.newLib.speeches.airplane2atc.PassingClearanceLimitNotification;

public interface IPilotPlaneWriter {
  void addExperience(Mood.ArrivalExperience experience);

  void addExperience(Mood.DepartureExperience experience);

  void changePilot(Pilot pilot, Airplane.State state);

  void sendMessage(ISpeech speech);

  void setState(Airplane.State state);

  void setTargetCoordinate(Coordinate coordinate);

  void setTargetHeading(int targetHeading);

  default void setTargetHeading(double targetHeading){
    this.setTargetHeading((int) Math.round(targetHeading));
  }

  void setTargetHeading(double heading, boolean isLeftTurned);

  void setTargetSpeed(int speed);
}
