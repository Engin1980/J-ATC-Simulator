package eng.jAtcSim.lib.world.approaches.entryLocations;

import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.geo.Headings;
import eng.jAtcSim.lib.world.Navaid;

public class FixRelatedApproachEntryLocation implements IApproachEntryLocation {
  private final Coordinate coordinate;
  private final double maximalDistance;
  private final double fromRadial;
  private final double toRadial;

  public FixRelatedApproachEntryLocation(Coordinate coordinate, double maximalDistance, double fromRadial, double toRadial) {
    this.coordinate = coordinate;
    this.maximalDistance = maximalDistance;
    this.fromRadial = fromRadial;
    this.toRadial = toRadial;
  }

  @Override
  public boolean isInside(Coordinate coordinate) {
    double radial = Coordinates.getBearing(this.coordinate, coordinate);
    if (Headings.isBetween(fromRadial, radial, toRadial)){
      double distance = Coordinates.getDistanceInNM(this.coordinate, coordinate);
      if (distance < maximalDistance)
        return true;
    }
    return false;
  }
}
