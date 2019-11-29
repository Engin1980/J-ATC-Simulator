package eng.jAtcSim.lib.airplanes.navigators;

import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.lib.airplanes.interfaces.modules.ISha4Navigator;
import eng.jAtcSim.lib.airplanes.modules.ShaModule;

public interface INavigator {
  void navigate(ISha4Navigator sha, Coordinate planeCoordinates);

}
