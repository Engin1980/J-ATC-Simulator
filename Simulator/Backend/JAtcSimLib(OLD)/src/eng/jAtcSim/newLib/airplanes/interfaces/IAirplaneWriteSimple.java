package eng.jAtcSim.newLib.area.airplanes.interfaces;

import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.area.airplanes.Airplane;
import eng.jAtcSim.newLib.area.airplanes.FlightRecorder;
import eng.jAtcSim.newLib.area.airplanes.behaviors.Behavior;
import eng.jAtcSim.newLib.area.airplanes.navigators.HeadingNavigator;
import eng.jAtcSim.newLib.area.airplanes.navigators.INavigator;
import eng.jAtcSim.newLib.area.atcs.Atc;
import eng.jAtcSim.newLib.global.Restriction;
import eng.jAtcSim.newLib.area.speaking.IFromAirplane;
import eng.jAtcSim.newLib.area.speaking.SpeechList;

public interface IAirplaneWriteSimple extends IAirplaneRO {

  void adjustTargetSpeed();

  void applyShortcut(Navaid navaid);

  IAirplaneWriteAdvanced getAdvanced();

  FlightRecorder getRecorderModule();

  default void sendMessage(IFromAirplane speech) {
    Atc atc = getAtcModule().getTunedAtc();
    sendMessage(atc, speech);
  }

  default void sendMessage(Atc atc, IFromAirplane speech) {
    SpeechList<IFromAirplane> lst = new SpeechList<>(speech);
    this.sendMessage(atc, lst);
  }

  void sendMessage(Atc atc, SpeechList speechList);

  void processRadarContactConfirmation();

  void setAltitudeRestriction(Restriction restriction);

  void setBehaviorAndState(Behavior behavior, Airplane.State state);

  void setNavigator(INavigator navigator);

  void setSpeedRestriction(Restriction speedRestriction);

  void setState(Airplane.State state);

  void setTargetAltitude(int altitude);

  void setTargetCoordinate(Coordinate coordinate);

  default void setTargetHeading(double heading) {
    this.setNavigator(
        new HeadingNavigator(heading));
  }

  default void setTargetHeading(double heading, boolean isLeftTurned) {
    this.setNavigator(
        new HeadingNavigator(
            heading,
            isLeftTurned ? HeadingNavigator.Turn.left : HeadingNavigator.Turn.right));
  }

  void setTargetSpeed(int ts);

  void setxState(Airplane.State state);

  void tuneAtc(Atc atc);
}
