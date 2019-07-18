package eng.jAtcSim.lib.airplanes.pilots.interfaces.forPilot;

import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.pilots.behaviors.Behavior;
import eng.jAtcSim.lib.airplanes.pilots.modules.PilotRecorderModule;
import eng.jAtcSim.lib.airplanes.pilots.navigators.HeadingNavigator;
import eng.jAtcSim.lib.airplanes.pilots.navigators.INavigator;
import eng.jAtcSim.lib.atcs.Atc;
import eng.jAtcSim.lib.global.Restriction;
import eng.jAtcSim.lib.speaking.IFromAirplane;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.world.Navaid;

public interface IPilotWriteSimple extends IPilotRO {
  void adjustTargetSpeed();

  void applyShortcut(Navaid navaid);

  IPilotWriteAdvanced getAdvanced();

  PilotRecorderModule getRecorderModule();

  default void passMessageToAtc(IFromAirplane speech) {
    Atc atc = getAtcModule().getTunedAtc();
    passMessageToAtc(atc, speech);
  }

  default void passMessageToAtc(Atc atc, IFromAirplane speech) {
    SpeechList<IFromAirplane> lst = new SpeechList<>(speech);
    this.passMessageToAtc(atc, lst);
  }

  void passMessageToAtc(Atc atc, SpeechList speechList);

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

  void tuneAtc(Atc atc);
}
