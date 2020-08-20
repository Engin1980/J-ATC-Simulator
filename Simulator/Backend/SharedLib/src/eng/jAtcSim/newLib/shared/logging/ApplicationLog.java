package eng.jAtcSim.newLib.shared.logging;


import eng.eSystem.events.EventAnonymous;
import eng.jAtcSim.newLib.shared.logging.writers.AutoNewLineLogWriter;
import eng.jAtcSim.newLib.shared.logging.writers.ConsoleWriter;
import eng.jAtcSim.newLib.shared.logging.writers.RealTimePipeLogWriter;

public class ApplicationLog {

  public static class AppLogMessage {
    public final String text;
    public final eType type;

    public AppLogMessage(String text, eType type) {
      this.text = text;
      this.type = type;
    }
  }

  private final Journal journal;

  public enum eType {
    info,
    warning,
    critical
  }

  public EventAnonymous<AppLogMessage> onNewMessage = new EventAnonymous<>();

  public ApplicationLog() {
    this.journal = new Journal(
        "Application log",
        false,
        new AutoNewLineLogWriter(
            new RealTimePipeLogWriter(
                new ConsoleWriter())));
  }

  public EventAnonymous<AppLogMessage> getOnNewMessage() {
    return onNewMessage;
  }

  public void write(eType type, String format, Object... params) {
    String s = String.format(format, params);
    this.journal.write("JAtcSim - %s: %s", type, s);
    onNewMessage.raise(new AppLogMessage(s, type));
  }
}
