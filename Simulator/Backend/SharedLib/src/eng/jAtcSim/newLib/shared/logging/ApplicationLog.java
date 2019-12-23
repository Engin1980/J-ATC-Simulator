package eng.jAtcSim.newLib.shared.logging;


import eng.eSystem.events.EventAnonymous;
import eng.jAtcSim.newLib.shared.logging.writers.AutoNewLineLogWriter;
import eng.jAtcSim.newLib.shared.logging.writers.ConsoleWriter;
import eng.jAtcSim.newLib.shared.logging.writers.RealTimePipeLogWriter;

public class ApplicationLog extends Log {

  public static class Message {
    public final String text;
    public final eType type;

    public Message(String text, eType type) {
      this.text = text;
      this.type = type;
    }
  }

  public enum eType {
    info,
    warning,
    critical
  }

  public EventAnonymous<Message> onNewMessage = new EventAnonymous<>();

  public ApplicationLog() {
    super("Application log",
        false,
        new AutoNewLineLogWriter(
            new RealTimePipeLogWriter(
                new ConsoleWriter())));
  }

  public EventAnonymous<Message> getOnNewMessage() {
    return onNewMessage;
  }

  public void writeLine(eType type, String format, Object... params) {
    String s = String.format(format, params);
    super.writeLine("JAtcSim - %s: %s", type, s);
    onNewMessage.raise(new Message(s, type));
  }
}
