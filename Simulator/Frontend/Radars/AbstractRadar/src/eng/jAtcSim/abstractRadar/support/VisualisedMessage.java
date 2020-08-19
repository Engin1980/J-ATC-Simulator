package eng.jAtcSim.abstractRadar.support;


import eng.jAtcSim.newLib.gameSim.Message;

public class VisualisedMessage {
  private final Message message;
  private int lifeCounter;

  public VisualisedMessage(Message message, int lifeCounter) {
    this.message = message;
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
}
