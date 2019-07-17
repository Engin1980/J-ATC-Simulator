package eng.jAtcSim.lib.airplanes.pilots.interfaces.forPilot;

import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.lib.airplanes.pilots.modules.PilotRecorderModule;
import eng.jAtcSim.lib.airplanes.pilots.navigators.HeadingNavigator;
import eng.jAtcSim.lib.airplanes.pilots.navigators.INavigator;
import eng.jAtcSim.lib.atcs.Atc;
import eng.jAtcSim.lib.speaking.IFromAirplane;
import eng.jAtcSim.lib.speaking.SpeechList;

public interface IPilotWriteSimple extends IPilotRO {
  void adjustTargetSpeed();

  IPilotWriteAdvanced getAdvanced();

  default void passMessageToAtc(IFromAirplane speech) {
    Atc atc = getAtcModule().getTunedAtc();
    passMessageToAtc(atc, speech);
  }

  default void passMessageToAtc(Atc atc, IFromAirplane speech) {
    SpeechList<IFromAirplane> lst = new SpeechList<>(speech);
    this.passMessageToAtc(atc, lst);
  }

  void passMessageToAtc(Atc atc, SpeechList speechList);

  void setNavigator(INavigator navigator);

  void setSpeedRestriction(Object o);

  void setTargetAltitude(int altitude);

  void setTargetCoordinate(Coordinate coordinate);

  default void setTargetHeading(double heading) {
    this.setNavigator(
        new HeadingNavigator(heading));
  }

  default void setTargetHeading(double heading, boolean isLeftTurned){
    this.setNavigator(
        new HeadingNavigator(
            heading,
            isLeftTurned ? HeadingNavigator.Turn.left : HeadingNavigator.Turn.right));
  }

  PilotRecorderModule getRecorderModule();
}
