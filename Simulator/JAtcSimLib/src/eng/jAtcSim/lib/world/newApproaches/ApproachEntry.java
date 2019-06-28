package eng.jAtcSim.lib.world.newApproaches;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.jAtcSim.lib.world.newApproaches.entryLocations.ApproachEntryLocation;

public class ApproachEntry {
  private final ApproachEntryLocation location;
  private final IList<IafRoute> route;

  public ApproachEntry(ApproachEntryLocation location, IList<IafRoute> route) {
    this.location = location;
    this.route = route;
  }

  public ApproachEntryLocation getLocation() {
    return location;
  }

  public IReadOnlyList<IafRoute> getRoute() {
    return route;
  }
}
