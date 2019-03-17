package eng.jAtcSim.lib.world.approaches;

import eng.eSystem.geo.Coordinate;

public abstract class ApproachEntryLocation {
  public abstract boolean isInside(Coordinate coordinate);
}
