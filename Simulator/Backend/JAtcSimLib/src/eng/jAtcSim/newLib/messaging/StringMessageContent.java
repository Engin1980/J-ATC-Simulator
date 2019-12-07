package eng.jAtcSim.newLib.messaging;

public class StringMessageContent implements IMessageContent {

  private final String messageText;

  public StringMessageContent(String messageText) {
    if (messageText == null) {
        throw new IllegalArgumentException("Value of {messageText} cannot not be null.");
    }

    this.messageText = messageText;
  }

  public StringMessageContent(String messageText, Object ... params){
    if (messageText == null) {
        throw new IllegalArgumentException("Value of {messageText} cannot not be null.");
    }

    String txt = String.format(messageText, params);
    this.messageText = txt;
  }

  public String getMessageText() {
    return messageText;
  }

  @Override
  public String toString() {
    return messageText + "{StringMessageContent}";
  }
}
