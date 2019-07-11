package eng.jAtcSim.lib.world.newApproaches.entryLocations;

import eng.eSystem.geo.Coordinate;

public interface IApproachEntryLocation {
  boolean isInside(Coordinate coordinate);
}
