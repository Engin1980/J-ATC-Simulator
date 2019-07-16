package eng.jAtcSim.lib.airplanes.pilots.interfaces.forPilot;

import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.lib.airplanes.pilots.interfaces.forAirplane.IAirplaneRO;
import eng.jAtcSim.lib.atcs.Atc;
import eng.jAtcSim.lib.speaking.IFromAirplane;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.world.Navaid;

public interface IPilot5 {
  void adjustTargetSpeed();

  IAtcModuleRO getAtcModule();

  IDivertModuleRO getDivertModule();

  IAirplaneRO getPlane();

  IRoutingModuleRO getRoutingModule();

  default void passMessageToAtc(Atc atc, IFromAirplane speech) {
    SpeechList<IFromAirplane> lst = new SpeechList<>(speech);
    this.passMessageToAtc(atc, lst);
  }

  void passMessageToAtc(Atc atc, SpeechList speechList);

  default void passMessageToAtc(IFromAirplane speech) {
    Atc atc = getAtcModule().getTunedAtc();
    passMessageToAtc(atc, speech);
  }

  void setHoldBehavior(Navaid navaid, int inboundRadial, boolean leftTurn);

  void setTargetAltitude(int altitude);

  void setTargetCoordinate(Coordinate coordinate);

  void setTargetHeading(double targetHeading);

  void setTargetHeading(double heading, boolean isLeftTurned);

}
