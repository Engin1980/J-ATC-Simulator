package eng.jAtcSim.lib.airplanes.pilots.modules;

import eng.eSystem.EStringBuilder;
import eng.eSystem.Tuple;
import eng.eSystem.collections.IReadOnlyList;
import eng.jAtcSim.lib.global.logging.AbstractSaver;
import eng.jAtcSim.lib.global.logging.Recorder;
import eng.jAtcSim.lib.speaking.ISpeech;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.afters.AfterCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.afters.AfterCommandList;

//TODO let this extends Module, probably?
public class PilotRecorderModule extends Recorder {

  public PilotRecorderModule(String recorderName, AbstractSaver os, String fromTimeSeparator) {
    super(recorderName, os, fromTimeSeparator);
  }

  public void logPostponedAfterSpeeches(AfterCommandList afterCommands) {
    IReadOnlyList<Tuple<AfterCommand, IAtcCommand>> tmp;
    tmp = afterCommands.getAsList(AfterCommandList.Type.route);
    _logPosponed(tmp, "route");
    tmp = afterCommands.getAsList(AfterCommandList.Type.extensions);
    _logPosponed(tmp, "extensions");
  }

  public void logProcessedAfterSpeeches(SpeechList<IAtcCommand> cmds, String extensions) {
    EStringBuilder sb = new EStringBuilder();
    sb.appendLine("Processed after speeches of " + extensions);
    for (int i = 0; i < cmds.size(); i++) {
      IAtcCommand cmd = cmds.get(i);
      sb.appendLine("\t").appendLine(cmd.toString()).appendLine();
    }
    super.writeLine(sb.toString());
  }

  public void logProcessedCurrentSpeeches(SpeechList current) {
    EStringBuilder sb = new EStringBuilder();
    sb.appendLine("Current processed speeches");
    for (int i = 0; i < current.size(); i++) {
      ISpeech sp = current.get(i);
      sb.append("\t").append(sp.toString()).appendLine();
    }
    super.writeLine(sb.toString());
  }

  private void _logPosponed(IReadOnlyList<Tuple<AfterCommand, IAtcCommand>> tmp, String type) {
    EStringBuilder sb = new EStringBuilder();
    sb.appendLine("Postponed " + type + " after commands");
    for (Tuple<AfterCommand, IAtcCommand> tuple : tmp) {
      sb.append("\t");
      sb.append(tuple.getA().toString());
      sb.append(" -> ");
      sb.append(tuple.getB().toString());
      sb.appendLine();
    }
    super.writeLine(sb.toString());
  }
}