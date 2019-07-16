package eng.jAtcSim.lib.airplanes.pilots.interfaces.forPilot;

import eng.jAtcSim.lib.airplanes.pilots.interfaces.forAirplane.IAirplaneRO;
import eng.jAtcSim.lib.atcs.Atc;
import eng.jAtcSim.lib.speaking.IFromAirplane;
import eng.jAtcSim.lib.speaking.SpeechList;

public interface IPilot5 {
  IAirplaneRO getPlane();

  void passMessageToAtc(Atc atc, SpeechList speechList);

  default void passMessageToAtc(Atc atc, IFromAirplane speech) {
    SpeechList<IFromAirplane> lst = new SpeechList<>(speech);
    this.passMessageToAtc(atc, lst);
  }

  IAtcModuleRO getAtcModule();

  default void passMessageToAtc(IFromAirplane speech) {
    Atc atc = getAtcModule().getTunedAtc();
    passMessageToAtc(atc, speech);
  }

}
