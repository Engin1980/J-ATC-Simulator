package eng.jAtcSim.newLib.airplanes.pilots.updaters;

import eng.eSystem.collections.*;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class ToCoordinateHeadingUpdater implements IHeadingUpdater {
  private final Coordinate coordinate;

  public ToCoordinateHeadingUpdater(Coordinate coordinate) {
    assert coordinate != null;
    this.coordinate = coordinate;
  }

  public void navigate(eng.jAtcSim.newLib.area.airplanes.interfaces.modules.ISha4Navigator sha, Coordinate planeCoordinates) {
    int heading = (int) Math.round(
        Coordinates.getBearing(planeCoordinates, this.coordinate));
    sha.setTargetHeading(heading);
  }

  public Coordinate getTargetCoordinate() {
    return coordinate;
  }
}
