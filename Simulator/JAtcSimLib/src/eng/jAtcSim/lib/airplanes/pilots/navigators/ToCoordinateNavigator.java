package eng.jAtcSim.lib.airplanes.pilots.navigators;

import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.jAtcSim.lib.airplanes.Airplane;

public class ToCoordinateNavigator implements INavigator {

  private final Coordinate coordinate;

  public ToCoordinateNavigator(Coordinate coordinate) {
    assert coordinate != null;
    this.coordinate = coordinate;
  }

  @Override
  public void navigate(Airplane.Airplane4Navigator plane) {
    int heading = (int) Math.round(
        Coordinates.getBearing(plane.getCoordinates(), this.coordinate));
    plane.setTargetHeading(heading);
  }
}
