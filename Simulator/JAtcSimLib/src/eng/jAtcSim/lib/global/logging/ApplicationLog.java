package eng.jAtcSim.lib.global.logging;


import eng.eSystem.events.EventAnonymous;

public class ApplicationLog extends Log {

  public static class Message{
    public final String text;
    public final eType type;

    public Message(String text, eType type) {
      this.text = text;
      this.type = type;
    }
  }

  public EventAnonymous<Message> onNewMessage = new EventAnonymous<>();

  public EventAnonymous<Message> getOnNewMessage() {
    return onNewMessage;
  }

  public ApplicationLog() {
    super("Application write", false, new WriterSaver(System.out, false));
  }

  public enum eType{
    info,
    warning,
    critical
  }

  public void writeLine(eType type, String format, Object ... params){
    String s = String.format(format,params);
    super.writeLine("JAtcSim - %s: %s", type, s);
    onNewMessage.raise(new Message(s, type));
  }
}
