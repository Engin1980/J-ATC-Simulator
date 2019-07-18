package eng.jAtcSim.lib.airplanes.navigators;

import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.lib.airplanes.modules.ShaModule;

public interface INavigator {
  void navigate(ShaModule sha, Coordinate planeCoordinates);
}
