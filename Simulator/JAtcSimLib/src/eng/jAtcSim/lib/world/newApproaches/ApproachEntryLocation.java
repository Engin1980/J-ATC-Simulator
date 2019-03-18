package eng.jAtcSim.lib.world.newApproaches;

import eng.eSystem.geo.Coordinate;

public abstract class ApproachEntryLocation {
  public abstract boolean isInside(Coordinate coordinate);
}
