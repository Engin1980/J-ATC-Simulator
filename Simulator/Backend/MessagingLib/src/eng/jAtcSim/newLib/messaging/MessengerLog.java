package eng.jAtcSim.newLib.messaging;

import eng.jAtcSim.newLib.shared.logging.LogFile;

public class MessengerLog {

  public enum eMessageAction {
    ADD,
    GET,
  }
  public enum eRegistrationAction{
    REGISTER,
    UNREGISTER
  }
  private final LogFile logFile;

  public MessengerLog(String file) {
    this.logFile = LogFile.openInDefaultPath (file);
  }

  public void recordRegistratin(eRegistrationAction action, Object key) {
    String line = String.format(
        "%S :: %s",
        action.toString(),
        key.toString()
    );
    this.logFile.write(line);
  }

  public void recordMessage(eMessageAction action, Message msg) {
    String line = String.format(
        "%S || FROM: %-10s; TO: %-10s; CONTENT: %s %n",
        action.toString(),
        msg.getSource().toString(),
        msg.getTarget().toString(),
        msg.getContent().toString()
    );
    this.logFile.write(line);
  }
}
