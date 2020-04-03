package eng.jAtcSim.newLib.area.approaches;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.area.approaches.locations.ILocation;
import eng.jAtcSim.newLib.area.routes.IafRoute;

public class ApproachEntry {
  public static ApproachEntry createDirect(ILocation entryLocation) {
    return new ApproachEntry(null, entryLocation);
  }

  public static ApproachEntry create(ILocation entryLocation, IafRoute iafRoute) {
    EAssert.Argument.isNotNull(iafRoute, "iafRoute");
    return new ApproachEntry(iafRoute, entryLocation);
  }

  private final IafRoute iafRoute;
  private final ILocation entryLocation;

  private ApproachEntry(IafRoute iafRoute, ILocation entryLocation) {
    EAssert.Argument.isNotNull(entryLocation, "entryLocation");
    this.iafRoute = iafRoute;
    this.entryLocation = entryLocation;
  }

  public ILocation getEntryLocation() {
    return entryLocation;
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
