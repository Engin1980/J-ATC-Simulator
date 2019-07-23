package eng.jAtcSim.lib.world.approaches.entryLocations;

import eng.eSystem.geo.Coordinate;

public interface IApproachEntryLocation {
  boolean isInside(Coordinate coordinate);
}
