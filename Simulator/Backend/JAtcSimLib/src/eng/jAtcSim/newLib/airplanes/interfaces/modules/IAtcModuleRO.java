package eng.jAtcSim.newLib.airplanes.interfaces.modules;

import eng.jAtcSim.newLib.atcs.Atc;

public interface IAtcModuleRO {
  int getSecondsWithoutRadarContact();

  Atc getTunedAtc();

  boolean hasRadarContact();
}
