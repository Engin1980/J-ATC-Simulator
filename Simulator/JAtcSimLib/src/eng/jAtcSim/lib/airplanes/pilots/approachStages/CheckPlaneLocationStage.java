package eng.jAtcSim.lib.airplanes.pilots.approachStages;

import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.geo.Headings;
import eng.eSystem.utilites.NumberUtils;
import eng.jAtcSim.lib.airplanes.pilots.behaviors.IPilot4Behavior;

public class CheckPlaneLocationStage extends CheckApproachStage {
  private final Coordinate coordinate;
  private final int minHeading;
  private final int maxHeading;
  private final double minDistance;
  private final int maxDistance;

  public CheckPlaneLocationStage(Coordinate coordinate, double minDistance, int maxDistance, int minHeading, int maxHeading) {
    this.coordinate = coordinate;
    this.minHeading = minHeading;
    this.maxHeading = maxHeading;
    this.minDistance = minDistance;
    this.maxDistance = maxDistance;
  }

  @Override
  protected eResult check(IPilot4Behavior pilot) {
    double realRadial = Coordinates.getBearing(coordinate, pilot.getCoordinate());
    if (Headings.isBetween(minHeading, realRadial, maxHeading) == false)
      return eResult.illegalHeading;
    else {
      double distance = Coordinates.getDistanceInNM(coordinate, pilot.getCoordinate());
      if (NumberUtils.isBetweenOrEqual(minDistance, distance, maxDistance) == false)
        return eResult.illegalDistance;
    }

    return eResult.ok;
  }
}
