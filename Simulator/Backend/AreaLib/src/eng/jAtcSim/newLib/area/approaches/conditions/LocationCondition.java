package eng.jAtcSim.newLib.area.approaches.conditions;

import eng.eSystem.collections.*;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.area.approaches.locations.ILocation;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class LocationCondition implements ICondition {
  private final ILocation location;

  public LocationCondition(ILocation location) {
    EAssert.Argument.isNotNull(location, "location");
    this.location = location;
  }
}
