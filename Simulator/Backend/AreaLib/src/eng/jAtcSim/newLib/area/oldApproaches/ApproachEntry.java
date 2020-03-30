package eng.jAtcSim.newLib.area.oldApproaches;

import eng.eSystem.collections.IList;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Headings;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.area.oldApproaches.entryLocations.FixRelatedApproachEntryLocation;
import eng.jAtcSim.newLib.area.oldApproaches.entryLocations.IApproachEntryLocation;
import eng.jAtcSim.newLib.area.routes.IafRoute;
import eng.jAtcSim.newLib.area.speeches.IAtcCommand;

public class ApproachEntry {
  public static ApproachEntry createForIls(ActiveRunwayThreshold threshold) {
    IApproachEntryLocation ael = new FixRelatedApproachEntryLocation(threshold.getCoordinate(), 15,
        Headings.subtract(threshold.getCourse(), 15),
        Headings.add(threshold.getCourse(),15));
    return new ApproachEntry(ael);
  }

  public static ApproachEntry createForIaf(IafRoute iafRoute) {
    IApproachEntryLocation ael = new FixRelatedApproachEntryLocation(
        iafRoute.getNavaid().getCoordinate(), 3, 0, 360);
    return new ApproachEntry(ael, iafRoute);
  }

  public static ApproachEntry createForUnprecise(Coordinate fafCoordinate, double inboundRadial) {
    IApproachEntryLocation ael = new FixRelatedApproachEntryLocation(fafCoordinate, 15,
        Headings.subtract(inboundRadial, 60),
        Headings.add(inboundRadial,60));
    return new ApproachEntry(ael);
  }

  public static ApproachEntry createForVisual(Airport parent, Navaid navaid, IApproachEntryLocation entryLocation, IList<IAtcCommand> iafRouteCommands) {
    IafRoute iafRoute = IafRoute.create(parent, navaid, iafRouteCommands);
    return new ApproachEntry(entryLocation, iafRoute);
  }

  private final IApproachEntryLocation location;
  private final IafRoute iafRoute;
//  private final IReadOnlyList<IAtcCommand> routeCommands;

  private ApproachEntry(IApproachEntryLocation location) {
    this.location = location;
    this.iafRoute = null;
  }
  private  ApproachEntry(IApproachEntryLocation location, IafRoute route){
    this.location = location;
    this.iafRoute = route;
  }

  public boolean isForCategory(char category){
    boolean ret;
    if (this.iafRoute == null)
      ret = true;
    else
      ret = this.iafRoute.getCategory().contains(category);
    return ret;
  }

  public IApproachEntryLocation getLocation() {
    return location;
  }

  public IafRoute getIafRoute() {
    return this.iafRoute;
  }
}