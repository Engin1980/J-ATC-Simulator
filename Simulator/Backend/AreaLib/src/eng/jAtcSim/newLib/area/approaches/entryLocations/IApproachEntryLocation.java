package eng.jAtcSim.newLib.area.approaches.entryLocations;

import eng.eSystem.geo.Coordinate;

public interface IApproachEntryLocation {
  boolean isInside(Coordinate coordinate);
}
