package eng.jAtcSim.lib.airplanes.pilots.interfaces.forAirplane;

import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.lib.global.Restriction;

public interface IShaRO {
  int getAltitude();

  int getTargetAltitude();

  Coordinate tryGetTargetCoordinate();

  Restriction getSpeedRestriction();

  int getSpeed();

  int getTargetHeading();

  int getHeading();
}
