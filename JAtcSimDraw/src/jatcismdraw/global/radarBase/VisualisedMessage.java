package jatcismdraw.global.radarBase;

import jatcsimlib.messaging.IMessageParticipant;

public class VisualisedMessage {

  private final IMessageParticipant source;
  private final String text;
  private int lifeCounter;

  public VisualisedMessage(IMessageParticipant source, String text, int lifeCounter) {
    this.source = source;
    this.text = text;
    this.lifeCounter = lifeCounter;
  }

  public IMessageParticipant getSource() {
    return source;
  }

  public String getText() {
    return text;
  }

  public int getLifeCounter() {
    return lifeCounter;
  }

  public void decreaseLifeCounter(){
    this.lifeCounter--;
  }
}
