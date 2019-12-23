package eng.jAtcSim.newLib.messaging;

import eng.jAtcSim.newLib.shared.logging.writers.AutoNewLineLogWriter;
import eng.jAtcSim.newLib.shared.logging.writers.FileWriter;
import eng.jAtcSim.newLib.shared.logging.writers.SimTimePipeLogWriter;

public class MessengerLog extends eng.jAtcSim.newLib.shared.logging.Log {

  public enum eAction {
    ADD,
    GET
  }

  public MessengerLog(String name, String file) {
    super(
        name,
        true,
        new AutoNewLineLogWriter(
            new SimTimePipeLogWriter(
                new FileWriter(file))));
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
