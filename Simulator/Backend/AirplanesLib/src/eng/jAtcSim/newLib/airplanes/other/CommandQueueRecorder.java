package eng.jAtcSim.newLib.airplanes.other;

import eng.jAtcSim.newLib.airplanes.contextLocal.Context;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.logging.LogFile;

public class CommandQueueRecorder extends AirplaneRecorder {

  private final LogFile journal;

  public CommandQueueRecorder(Callsign callsign) {
    super(callsign);
    this.journal = LogFile.openInDefaultPath(callsign.toString() + ".cqr.txt");
  }

  public void log(String commandLinesText) {

    this.journal.write("%s; \t %s %n",
            Context.getShared().getNow().toString(),
            commandLinesText);
  }
}
