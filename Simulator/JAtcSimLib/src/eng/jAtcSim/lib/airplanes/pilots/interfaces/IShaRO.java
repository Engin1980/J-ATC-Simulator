package eng.jAtcSim.lib.airplanes.pilots.interfaces;

import eng.eSystem.geo.Coordinate;

public interface IShaRO {
  int getAltitude();

  int getTargetAltitude();

  Coordinate tryGetTargetCoordinate();
}
