package eng.jAtcSim.abstractRadar.support;


import eng.jAtcSim.newLib.gameSim.IMessage;

public class VisualisedMessage {
  private final IMessage message;
  private int lifeCounter;

  public VisualisedMessage(IMessage message, int lifeCounter) {
    this.message = message;
    this.lifeCounter = lifeCounter;
  }

  public void decreaseLifeCounter() {
    this.lifeCounter--;
  }

  public int getLifeCounter() {
    return lifeCounter;
  }

  public IMessage getMessage() {
    return this.message;
  }
}
