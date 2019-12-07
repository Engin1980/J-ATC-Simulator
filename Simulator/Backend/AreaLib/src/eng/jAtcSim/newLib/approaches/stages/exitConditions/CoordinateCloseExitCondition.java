package eng.jAtcSim.newLib.approaches.stages.exitConditions;

import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.approaches.stages.IExitCondition;

public class CoordinateCloseExitCondition implements IExitCondition {
  private final Coordinate coordinate;
  private final double distance;

  public CoordinateCloseExitCondition(Coordinate coordinate, double distance) {
    this.coordinate = coordinate;
    this.distance = distance;
  }

  public Coordinate getCoordinate() {
    return coordinate;
  }

  public double getDistance() {
    return distance;
  }
}
