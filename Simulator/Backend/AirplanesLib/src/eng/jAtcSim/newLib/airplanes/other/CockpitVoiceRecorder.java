package eng.jAtcSim.newLib.airplanes.other;

import eng.eSystem.EStringBuilder;
import eng.jAtcSim.newLib.messaging.Message;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.logging.Journal;
import eng.jAtcSim.newLib.shared.logging.writers.*;
import eng.jAtcSim.newLib.speeches.base.ISpeech;
import eng.jAtcSim.newLib.speeches.SpeechList;

public class CockpitVoiceRecorder extends AirplaneRecorder {

  private final static String SEPARATOR = " ; ";
  private final Journal journal;

  public CockpitVoiceRecorder(Callsign callsign) {
    super(callsign);
    ILogWriter wrt;
    wrt = new FileWriter("R:\\" + callsign.toString() + ".cvr.txt");
    wrt = new AutoNewLineLogWriter(wrt);
    wrt = new SimTimePipeLogWriter(wrt);
    wrt = new RealTimePipeLogWriter(wrt);
    this.journal = new Journal(callsign.toString() + " (CVR)", true, wrt);
  }

  public void log(Message m) {

    String src = m.getSource().toString();
    String trg = m.getTarget().toString();
    String cnt = m.getContent().toString(); //toLogString();

    journal.write(
        "FROM: %s %s TO: %s %s %s",
        src, SEPARATOR, trg, SEPARATOR, cnt);
  }

  public void logProcessedAfterSpeeches(SpeechList<? extends ISpeech> cmds, String extensions) {
    EStringBuilder sb = new EStringBuilder();
    sb.appendLine("Processed after speeches of " + extensions);
    for (int i = 0; i < cmds.size(); i++) {
      ISpeech cmd = cmds.get(i);
      sb.appendLine("\t").appendLine(cmd.toString()).appendLine();
    }
    journal.write(sb.toString());
  }

  public void logProcessedCurrentSpeeches(SpeechList current) {
    EStringBuilder sb = new EStringBuilder();
    sb.appendLine("Current processed speeches");
    for (int i = 0; i < current.size(); i++) {
      ISpeech sp = current.get(i);
      sb.append("\t").append(sp.toString()).appendLine();
    }
    journal.write(sb.toString());
  }

}
