package eng.jAtcSim.newLib.area.airplanes.navigators;

import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.jAtcSim.newLib.area.airplanes.interfaces.modules.ISha4Navigator;

public class ToCoordinateNavigator implements INavigator2Coordinate {

  private final Coordinate coordinate;

  public ToCoordinateNavigator(Coordinate coordinate) {
    assert coordinate != null;
    this.coordinate = coordinate;
  }

  @Override
  public void navigate(ISha4Navigator sha, Coordinate planeCoordinates) {
    int heading = (int) Math.round(
        Coordinates.getBearing(planeCoordinates, this.coordinate));
    sha.setTargetHeading(heading);
  }

  @Override
  public Coordinate getTargetCoordinate() {
    return coordinate;
  }
}
