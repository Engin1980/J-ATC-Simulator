package eng.jAtcSim.newLib.airplanes.other;

import eng.eSystem.EStringBuilder;
import eng.jAtcSim.newLib.airplanes.contextLocal.Context;
import eng.jAtcSim.newLib.messaging.Message;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.logging.LogFile;
import eng.jAtcSim.newLib.speeches.SpeechList;
import eng.jAtcSim.newLib.speeches.base.ISpeech;

public class CockpitVoiceRecorder extends AirplaneRecorder {

  private final static String SEPARATOR = " ; ";
  private final LogFile logFile;

  public CockpitVoiceRecorder(Callsign callsign) {
    super(callsign);

    this.logFile = LogFile.openInDefaultPath(callsign.toString() + ".cvr.txt");
  }

  public void log(Message m) {

    String src = m.getSource().toString();
    String trg = m.getTarget().toString();
    String cnt = m.getContent().toString();

    logFile.write(
            "%s\tFROM: %s %s TO: %s %s %s%n",
            Context.getShared().getNow().toString(),
            src, SEPARATOR, trg, SEPARATOR, cnt);
  }

  public void logProcessedAfterSpeeches(SpeechList<? extends ISpeech> cmds, String extensions) {
    EStringBuilder sb = new EStringBuilder();
    if (cmds.size() > 0) {
      sb.appendLine("Processed after speeches of " + extensions);
      for (int i = 0; i < cmds.size(); i++) {
        ISpeech cmd = cmds.get(i);
        sb.appendLine("\t").appendLine(cmd.toString()).appendLine();
      }
      logFile.write(sb.toString());
    }
  }

  public void logProcessedCurrentSpeeches(SpeechList current) {
    EStringBuilder sb = new EStringBuilder();
    if (current.size() > 0) {
      sb.appendLine("Current processed speeches");
      for (int i = 0; i < current.size(); i++) {
        ISpeech sp = current.get(i);
        sb.append("\t").append(sp.toString()).appendLine();
      }
      logFile.write(sb.toString());
    }
  }

}
