package eng.jAtcSim.lib.airplanes.pilots.approachStages;

import eng.eSystem.collections.*;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.xmlSerialization.annotations.XmlOptional;

public class CheckPlaneLocationStage implements IApproachStage{
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
}
