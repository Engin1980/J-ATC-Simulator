package eng.jAtcSim.lib.messaging;

import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.global.logging.Recorder;

public class MessengerRecorder extends Recorder {

  public enum eAction {
    ADD,
    GET
  }

  public MessengerRecorder(String name, String file) {
    super(
        name,
        Recorder.createRecorderFileOutputStream(file), "; ");
  }

  public void recordMessage(eAction action, Message msg) {
    String line = String.format(
        "%S || FROM: %-10s; TO: %-10s; CONTENT: %s\n",
        action.toString(),
        msg.getSource().toString(),
        msg.getTarget().toString(),
        msg.getContent().toString()
    );
    super.writeLine(line);
  }
}
