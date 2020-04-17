package eng.jAtcSim.newLib.airplanes.other;

import eng.eSystem.EStringBuilder;
import eng.jAtcSim.newLib.messaging.Message;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.logging.writers.*;
import eng.jAtcSim.newLib.speeches.ICommand;
import eng.jAtcSim.newLib.speeches.ISpeech;
import eng.jAtcSim.newLib.speeches.SpeechList;

import java.io.IOException;

public class CockpitVoiceRecorder extends AirplaneRecorder {

  private final ILogWriter writer;

  public CockpitVoiceRecorder(Callsign callsign) {
    super(callsign);
    this.writer = new RealTimePipeLogWriter(
        new SimTimePipeLogWriter(
            new AutoNewLineLogWriter(
                new FileWriter("R:\\" + callsign.toString() + ".cvr.txt"))));
  }

  private final static String SEPARATOR = " ; ";

  public void log(Message m) {
    EStringBuilder sb = new EStringBuilder();

    String src = m.getSource().toString();
    String trg = m.getTarget().toString();
    String cnt = m.getContent().toString(); //toLogString();

    sb.appendFormat("FROM: %s ", src).append(SEPARATOR);
    sb.appendFormat("TO: %s ", trg).append(SEPARATOR);
    sb.appendFormat(" %s ", cnt);

    try {
      writer.writeLine(sb.toString());
    } catch (IOException e) {
      logErrorToAppLog(new IOException(this.callsign.toString() + " CVR write failed.", e));
    }
  }

  public void logProcessedCurrentSpeeches(SpeechList current) {
    EStringBuilder sb = new EStringBuilder();
    sb.appendLine("Current processed speeches");
    for (int i = 0; i < current.size(); i++) {
      ISpeech sp = current.get(i);
      sb.append("\t").append(sp.toString()).appendLine();
    }
    try {
      writer.writeLine(sb.toString());
    } catch (IOException e) {
      logErrorToAppLog(new IOException(this.callsign.toString() + " CVR write failed.", e));
    }
  }

  public void logProcessedAfterSpeeches(SpeechList<? extends ISpeech> cmds, String extensions) {
    EStringBuilder sb = new EStringBuilder();
    sb.appendLine("Processed after speeches of " + extensions);
    for (int i = 0; i < cmds.size(); i++) {
      ISpeech cmd = cmds.get(i);
      sb.appendLine("\t").appendLine(cmd.toString()).appendLine();
    }
    try {
      writer.writeLine(sb.toString());
    } catch (IOException e) {
      logErrorToAppLog(new IOException(this.callsign.toString() + " CVR write failed.", e));
    }
  }

}
