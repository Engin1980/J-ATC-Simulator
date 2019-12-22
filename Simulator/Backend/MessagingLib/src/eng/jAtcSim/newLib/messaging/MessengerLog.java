package eng.jAtcSim.newLib.messaging;

import eng.jAtcSim.newLib.global.logging.FileSaver;
import eng.jAtcSim.newLib.global.logging.Recorder;

public class MessengerLog extends Recorder {

  public enum eAction {
    ADD,
    GET
  }

  public MessengerLog(String name, String file) {
    super(
        name,
        new FileSaver(Recorder.getRecorderFileName(file)),
        "; ");
  }

  public void recordMessage(eAction action, Message msg) {
    String line = String.format(
        "%S || FROM: %-10s; TO: %-10s; CONTENT: %s",
        action.toString(),
        msg.getSource().toString(),
        msg.getTarget().toString(),
        msg.getContent().toString()
    );
    super.writeLine(line);
  }
}
