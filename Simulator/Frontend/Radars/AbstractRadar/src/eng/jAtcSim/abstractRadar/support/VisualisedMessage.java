package eng.jAtcSim.abstractRadar.support;


import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.messaging.Message;

public class VisualisedMessage {
  private final Message message;
  private final String messageText;
  private int lifeCounter;

  public VisualisedMessage(Message message, String messageText, int lifeCounter) {
    EAssert.Argument.isNotNull(message, "message");
    EAssert.Argument.isNotNull(messageText, "messageText");
    this.message = message;
    this.messageText = messageText;
    this.lifeCounter = lifeCounter;
  }

  public void decreaseLifeCounter() {
    this.lifeCounter--;
  }

  public int getLifeCounter() {
    return lifeCounter;
  }

  public Message getMessage() {
    return this.message;
  }

  public String getMessageText() {
    return messageText;
  }
}
