package eng.jAtcSim.lib.messaging;

import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.global.Recorder;

import java.nio.file.Path;

public class MessageRecorder extends Recorder {

  public enum eAction{
    ADD,
    GET
  }

  public MessageRecorder(String filePath, boolean toConsole, boolean autoOpenFile) {
    super(Recorder.buildGenericLogFilePath(filePath), toConsole, autoOpenFile);
  }

  public void recordMessage(eAction action, Message msg){
    String line = String.format(
        "%S; %S || FROM: %-10s; TO: %-10s; CONTENT: %s\n",
        Acc.sim().getNow().toTimeString(),
        action.toString(),
        msg.getSource().toString(),
        msg.getTarget().toString(),
        msg.getContent().toString()
    );

    super.logLine(
        line);
  }
}
