package eng.jAtcSim.newLib.area.approaches;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.area.approaches.locations.ILocation;
import eng.jAtcSim.newLib.area.routes.IafRoute;

public class ApproachEntry {
private final IafRoute iafRoute;
private final ILocation entryLocation;

  public ApproachEntry(IafRoute iafRoute, ILocation entryLocation) {
    EAssert.Argument.isNotNull(iafRoute, "iafRoute");
    EAssert.Argument.isNotNull(entryLocation, "entryLocation");
    this.iafRoute = iafRoute;
    this.entryLocation = entryLocation;
  }

  public IafRoute getIafRoute() {
    return iafRoute;
  }

  public ILocation getEntryLocation() {
    return entryLocation;
  }
}
