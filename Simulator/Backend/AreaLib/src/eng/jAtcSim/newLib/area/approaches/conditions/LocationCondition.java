package eng.jAtcSim.newLib.area.approaches.conditions;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.area.approaches.locations.ILocation;

public class LocationCondition implements ICondition {
  public static LocationCondition create(ILocation location) {
    return new LocationCondition(location);
  }

  private final ILocation location;

  private LocationCondition(ILocation location) {
    EAssert.Argument.isNotNull(location, "location");
    this.location = location;
  }

  public ILocation getLocation() {
    return location;
  }

  @Override
  public String toString() {
    return "LocationCondition{" + '}';
  }
}
