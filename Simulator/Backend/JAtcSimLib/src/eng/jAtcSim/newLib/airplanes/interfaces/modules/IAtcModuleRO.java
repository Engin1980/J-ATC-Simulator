package eng.jAtcSim.newLib.area.airplanes.interfaces.modules;

import eng.jAtcSim.newLib.area.atcs.Atc;

public interface IAtcModuleRO {
  int getSecondsWithoutRadarContact();

  Atc getTunedAtc();

  boolean hasRadarContact();
}
