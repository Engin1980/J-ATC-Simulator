package eng.jAtcSim.newLib.area.approaches.conditions.locations;

import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.area.approaches.conditions.ICondition;

public interface ILocation extends ICondition {
  boolean isInside(Coordinate coordinate);
}
