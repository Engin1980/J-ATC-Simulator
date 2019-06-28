package eng.jAtcSim.lib.world.newApproaches.entryLocations;

import eng.eSystem.geo.Coordinate;

public abstract class ApproachEntryLocation {
  public abstract boolean isInside(Coordinate coordinate);
}
