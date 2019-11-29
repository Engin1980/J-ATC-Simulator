package eng.jAtcSim.lib.area.approaches.entryLocations;

import eng.eSystem.geo.Coordinate;

public interface IApproachEntryLocation {
  boolean isInside(Coordinate coordinate);
}
