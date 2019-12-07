package eng.jAtcSim.newLib.approaches.stages.checks;

import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.approaches.stages.ICheckStage;

public class CheckPlaneLocationStage implements ICheckStage {
  private final Coordinate coordinate;
  private final int fromInboundRadial;
  private final int toInboundRadial;
  private final double minDistance;
  private final double maxDistance;

  public CheckPlaneLocationStage(Coordinate coordinate, double minDistance, double maxDistance, int fromInboundRadial, int toInboundRadial) {
    this.coordinate = coordinate;
    this.fromInboundRadial = fromInboundRadial;
    this.toInboundRadial = toInboundRadial;
    this.minDistance = minDistance;
    this.maxDistance = maxDistance;
  }

  public CheckPlaneLocationStage(Coordinate coordinate, double maxDistance) {
    this(coordinate, 0, maxDistance, 0, 360);
  }


  public Coordinate getCoordinate() {
    return coordinate;
  }

  public int getFromInboundRadial() {
    return fromInboundRadial;
  }

  public int getToInboundRadial() {
    return toInboundRadial;
  }

  public double getMinDistance() {
    return minDistance;
  }

  public double getMaxDistance() {
    return maxDistance;
  }

  //
//  @Override
//  protected eResult check(IPilot5Behavior pilot) {
//    double realRadial = Coordinates.getBearing(coordinate, pilot.getCoordinate());
//    if (Headings.isBetween(fromInboundRadial, realRadial, toInboundRadial) == false)
//      return eResult.illegalHeading;
//    else {
//      double distance = Coordinates.getDistanceInNM(coordinate, pilot.getCoordinate());
//      if (NumberUtils.isBetweenOrEqual(minDistance, distance, maxDistance) == false)
//        return eResult.illegalLocation;
//    }
//
//    return eResult.ok;
//  }
}
