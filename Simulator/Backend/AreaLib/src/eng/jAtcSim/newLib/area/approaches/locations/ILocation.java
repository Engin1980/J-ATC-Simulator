package eng.jAtcSim.newLib.area.approaches.locations;

import eng.eSystem.geo.Coordinate;

public interface ILocation {
  boolean isInside(Coordinate coordinate);
}
