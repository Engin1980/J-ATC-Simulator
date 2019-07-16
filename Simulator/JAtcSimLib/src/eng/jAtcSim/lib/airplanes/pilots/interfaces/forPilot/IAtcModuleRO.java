package eng.jAtcSim.lib.airplanes.pilots.interfaces.forPilot;

import eng.jAtcSim.lib.atcs.Atc;

public interface IAtcModuleRO {
  int getSecondsWithoutRadarContact();

  Atc getTunedAtc();
}
