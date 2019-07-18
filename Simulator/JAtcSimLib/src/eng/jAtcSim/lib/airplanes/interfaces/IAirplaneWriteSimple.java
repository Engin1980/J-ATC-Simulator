package eng.jAtcSim.lib.airplanes.interfaces;

import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.FlightRecorder;
import eng.jAtcSim.lib.airplanes.modules.PilotRecorderModule;
import eng.jAtcSim.lib.airplanes.behaviors.Behavior;
import eng.jAtcSim.lib.airplanes.navigators.HeadingNavigator;
import eng.jAtcSim.lib.airplanes.navigators.INavigator;
import eng.jAtcSim.lib.atcs.Atc;
import eng.jAtcSim.lib.global.Restriction;
import eng.jAtcSim.lib.speaking.IFromAirplane;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.world.Navaid;

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
