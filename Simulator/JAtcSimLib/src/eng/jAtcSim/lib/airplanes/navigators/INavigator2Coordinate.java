package eng.jAtcSim.lib.airplanes.navigators;

import eng.eSystem.geo.Coordinate;

public interface INavigator2Coordinate extends INavigator {
  Coordinate getTargetCoordinate();
}