package eng.jAtcSim.lib.airplanes.interfaces.modules;

import eng.jAtcSim.lib.atcs.Atc;

public interface IAtcModuleRO {
  int getSecondsWithoutRadarContact();

  Atc getTunedAtc();

  boolean hasRadarContact();
}
