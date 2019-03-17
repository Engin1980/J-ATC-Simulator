package eng.jAtcSim.lib.world.approaches;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.geo.Coordinate;

public class RegionalApproachEntryLocation extends ApproachEntryLocation {
  private final IList<Coordinate> points;

  public RegionalApproachEntryLocation(IList<Coordinate> points) {
    this.points = points;
  }

  public RegionalApproachEntryLocation(Coordinate ... points) {
    this.points = new EList<>(points);
  }

  @Override
  public boolean isInside(Coordinate coordinate) {
    throw new UnsupportedOperationException("Impement");
  }
}
