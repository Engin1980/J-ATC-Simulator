package eng.jAtcSim.newLib.area.approaches.conditions;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.area.approaches.locations.ILocation;

public class LocationCondition implements ICondition {
  private final ILocation location;

  public static LocationCondition create(ILocation location){
    return new LocationCondition(location);
  }

  public LocationCondition(ILocation location) {
    EAssert.Argument.isNotNull(location, "location");
    this.location = location;
  }

  public ILocation getLocation() {
    return location;
  }
}
