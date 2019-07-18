package eng.jAtcSim.lib.airplanes.interfaces.modules;

import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.lib.global.Restriction;

public interface IShaRO {
  int getAltitude();

  int getTargetAltitude();

  int getTargetSpeed();

  Coordinate tryGetTargetCoordinate();

  Restriction getSpeedRestriction();

  int getSpeed();

  int getTargetHeading();

  int getHeading();
}
