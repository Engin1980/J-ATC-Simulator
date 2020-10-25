package eng.jAtcSim.newLib.airplanes.other;

import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.logging.Journal;
import eng.jAtcSim.newLib.shared.logging.writers.*;

public class CommandQueueRecorder extends AirplaneRecorder {

  private final Journal journal;

  public CommandQueueRecorder(Callsign callsign) {
    super(callsign);
    ILogWriter wrt = FileWriter.createToDefaultFolder(callsign.toString() + ".cqr.txt");
    wrt = new AutoNewLineLogWriter(wrt);
    wrt = new SimTimePipeLogWriter(wrt);
    wrt = new RealTimePipeLogWriter(wrt);

    this.journal = new Journal(callsign.toString() + " (CQR)", true, wrt);
  }

  public void log(String commandLinesText) {
    this.journal.write("\n" + commandLinesText);
  }
}
