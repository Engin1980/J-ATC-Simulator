package eng.jAtcSim.lib.airplanes.pilots.navigators;

import eng.eSystem.geo.Coordinate;

public interface INavigator2Coordinate extends INavigator {
  Coordinate getTargetCoordinate();
}
