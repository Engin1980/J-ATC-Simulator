package eng.jAtcSim.lib.world.approaches;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Headings;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ChangeHeadingCommand;
import eng.jAtcSim.lib.world.approaches.entryLocations.FixRelatedApproachEntryLocation;
import eng.jAtcSim.lib.world.approaches.entryLocations.IApproachEntryLocation;
import eng.jAtcSim.lib.world.approaches.entryLocations.RegionalApproachEntryLocation;

public class ApproachEntry {
  public static ApproachEntry createForIls(Coordinate thresholdCoordinate, int runwayThresholdCourse) {
    IApproachEntryLocation ael = new FixRelatedApproachEntryLocation(thresholdCoordinate, 15,
        Headings.subtract(runwayThresholdCourse, 15),
        Headings.add(runwayThresholdCourse,15));
    return new ApproachEntry(ael);
  }

  public static ApproachEntry createForIaf(IafRoute iafRoute) {
    IApproachEntryLocation ael = new FixRelatedApproachEntryLocation(
        iafRoute.getNavaid().getCoordinate(), 3, 0, 360);
    return new ApproachEntry(ael, iafRoute);
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

  public IApproachEntryLocation getLocation() {
    return location;
  }

  public IafRoute getIafRoute() {
    return this.iafRoute;
  }
}
