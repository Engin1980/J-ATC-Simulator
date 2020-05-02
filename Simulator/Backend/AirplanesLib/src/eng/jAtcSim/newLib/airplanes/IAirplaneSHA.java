package eng.jAtcSim.newLib.airplanes;

import eng.jAtcSim.newLib.shared.Restriction;

public interface IAirplaneSHA {
  int getAltitude();

  int getHeading();

  int getSpeed();

  Restriction getSpeedRestriction();

  int getTargetAltitude();

  int getTargetHeading();
}
