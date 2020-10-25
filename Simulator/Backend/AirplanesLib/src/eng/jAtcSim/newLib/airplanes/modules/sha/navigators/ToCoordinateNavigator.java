package eng.jAtcSim.newLib.airplanes.modules.sha.navigators;

import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.jAtcSim.newLib.airplanes.IAirplane;
import eng.jAtcSim.newLib.shared.enums.LeftRight;

public class ToCoordinateNavigator extends Navigator {
  @Override
  public NavigatorResult navigate(IAirplane plane) {
    int heading = (int) Math.round(
        Coordinates.getBearing(plane.getCoordinate(), this.coordinate));
    LeftRight turn = getBetterDirectionToTurn(plane.getSha().getHeading(), heading);
    return new NavigatorResult(heading, turn);
  }

  private final Coordinate coordinate;

  public ToCoordinateNavigator(Coordinate coordinate) {
    assert coordinate != null;
    this.coordinate = coordinate;
  }

  public Coordinate getTargetCoordinate() {
    return coordinate;
  }

  @Override
  public String toString() {
    return "ToCoordinateNavigator{" +
            "coordinate=" + coordinate +
            '}';
  }
}
