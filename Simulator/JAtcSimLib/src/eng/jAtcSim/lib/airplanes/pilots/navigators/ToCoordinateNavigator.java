package eng.jAtcSim.lib.airplanes.pilots.navigators;

import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.jAtcSim.lib.airplanes.modules.ShaModule;

public class ToCoordinateNavigator implements INavigator2Coordinate {

  private final Coordinate coordinate;

  public ToCoordinateNavigator(Coordinate coordinate) {
    assert coordinate != null;
    this.coordinate = coordinate;
  }

  @Override
  public void navigate(ShaModule sha, Coordinate planeCoordinates) {
    int heading = (int) Math.round(
        Coordinates.getBearing(planeCoordinates, this.coordinate));
    sha._setTargetHeading(heading);
  }

  @Override
  public Coordinate getTargetCoordinate() {
    return coordinate;
  }
}
