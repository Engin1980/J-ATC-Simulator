package eng.jAtcSim.newLib.airplanes.navigators;

import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.airplanes.interfaces.modules.ISha4Navigator;
import eng.jAtcSim.newLib.airplanes.modules.ShaModule;

public interface INavigator {
  void navigate(ISha4Navigator sha, Coordinate planeCoordinates);

}
