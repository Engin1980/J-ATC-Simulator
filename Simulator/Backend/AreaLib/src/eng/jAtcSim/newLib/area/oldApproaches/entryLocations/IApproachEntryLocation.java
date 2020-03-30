package eng.jAtcSim.newLib.area.oldApproaches.entryLocations;

import eng.eSystem.geo.Coordinate;

public interface IApproachEntryLocation {
  boolean isInside(Coordinate coordinate);
}
