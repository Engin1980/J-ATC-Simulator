package eng.jAtcSim.newLib.messaging;

import eng.jAtcSim.newLib.shared.logging.Journal;
import eng.jAtcSim.newLib.shared.logging.writers.AutoNewLineLogWriter;
import eng.jAtcSim.newLib.shared.logging.writers.FileWriter;
import eng.jAtcSim.newLib.shared.logging.writers.SimTimePipeLogWriter;

public class MessengerLog {

  public enum eMessageAction {
    ADD,
    GET,
  }
  public enum eRegistrationAction{
    REGISTER,
    UNREGISTER
  }
  private Journal journal;

  public MessengerLog(String file) {
    this.journal = new Journal("Messenger log", true,
        new AutoNewLineLogWriter(
            new SimTimePipeLogWriter(
                FileWriter.createToDefaultFolder(file))));
  }

  public void recordRegistratin(eRegistrationAction action, Object key) {
    String line = String.format(
        "%S :: %s",
        action.toString(),
        key.toString()
    );
    this.journal.write(line);
  }

  public void recordMessage(eMessageAction action, Message msg) {
    String line = String.format(
        "%S || FROM: %-10s; TO: %-10s; CONTENT: %s",
        action.toString(),
        msg.getSource().toString(),
        msg.getTarget().toString(),
        msg.getContent().toString()
    );
    this.journal.write(line);
  }
}
