package eng.jAtcSim.lib.world.newApproaches;

import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.world.newApproaches.entryLocations.IApproachEntryLocation;

public class ApproachEntry {
  private final IApproachEntryLocation location;
  private final SpeechList<IAtcCommand> routeCommands;

  public ApproachEntry(IApproachEntryLocation location, SpeechList<IAtcCommand> routeCommands) {
    this.location = location;
    this.routeCommands = routeCommands;
  }

  public IApproachEntryLocation getLocation() {
    return location;
  }

  public SpeechList<IAtcCommand> getRouteCommands() {
    return routeCommands;
  }
}
