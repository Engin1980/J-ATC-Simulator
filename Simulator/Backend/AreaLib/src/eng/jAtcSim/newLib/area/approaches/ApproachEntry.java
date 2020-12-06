package eng.jAtcSim.newLib.area.approaches;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.area.approaches.conditions.ICondition;
import eng.jAtcSim.newLib.area.approaches.conditions.locations.ILocation;
import eng.jAtcSim.newLib.area.routes.IafRoute;

public class ApproachEntry {
  public static ApproachEntry createDirect(ICondition entryCondition) {
    return new ApproachEntry(null, entryCondition);
  }

  public static ApproachEntry create(ICondition entryCondition, IafRoute iafRoute) {
    EAssert.Argument.isNotNull(iafRoute, "iafRoute");
    return new ApproachEntry(iafRoute, entryCondition);
  }

  private final IafRoute iafRoute;
  private final ICondition entryCondition;

  private ApproachEntry(IafRoute iafRoute, ICondition entryCondition) {
    EAssert.Argument.isNotNull(entryCondition, "entryCondition");
    this.iafRoute = iafRoute;
    this.entryCondition = entryCondition;
  }

  public ICondition getEntryCondition() {
    return entryCondition;
  }

  public IafRoute getIafRoute() {
    return iafRoute;
  }

  public boolean isForCategory(char c){
    if (iafRoute == null)
      return true;
    else
      return iafRoute.getCategory().contains(c);
  }
}
