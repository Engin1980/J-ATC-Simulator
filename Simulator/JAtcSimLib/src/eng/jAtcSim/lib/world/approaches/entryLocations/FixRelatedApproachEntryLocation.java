package eng.jAtcSim.lib.world.approaches.entryLocations;

import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.geo.Headings;
import eng.jAtcSim.lib.world.Navaid;

public class FixRelatedApproachEntryLocation implements IApproachEntryLocation {
  private final Navaid navaid;
  private final double maximalDistance;
  private final double fromRadial;
  private final double toRadial;

  public FixRelatedApproachEntryLocation(Navaid navaid, double maximalDistance, double fromRadial, double toRadial) {
    this.navaid = navaid;
    this.maximalDistance = maximalDistance;
    this.fromRadial = fromRadial;
    this.toRadial = toRadial;
  }

  @Override
  public boolean isInside(Coordinate coordinate) {
    double radial = Coordinates.getBearing(navaid.getCoordinate(), coordinate);
    if (Headings.isBetween(fromRadial, radial, toRadial)){
      double distance = Coordinates.getDistanceInNM(navaid.getCoordinate(), coordinate);
      if (distance < maximalDistance)
        return true;
    }
    return false;
  }
}
