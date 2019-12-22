package eng.jAtcSim.newLib.area.airplanes.navigators;

import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.area.airplanes.interfaces.modules.ISha4Navigator;

public interface INavigator {
  void navigate(ISha4Navigator sha, Coordinate planeCoordinates);

}
