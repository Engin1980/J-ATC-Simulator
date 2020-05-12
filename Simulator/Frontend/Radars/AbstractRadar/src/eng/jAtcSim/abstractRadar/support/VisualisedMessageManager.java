package eng.jAtcSim.abstractRadar.support;

import eng.jAtcSim.abstractRadar.published.IMessage;

import java.util.ArrayList;
import java.util.List;

public class VisualisedMessageManager {
  private final int delay;
  private List<VisualisedMessage> items = new ArrayList<>();

  public VisualisedMessageManager(int delay) {
    this.delay = delay;
  }

  public void add(IMessage message) {
    VisualisedMessage di = new VisualisedMessage(message, delay);
    items.add(di);
  }

  public void decreaseMessagesLifeCounter() {
    for (VisualisedMessage item : items) {
      item.decreaseLifeCounter();
    }
    items.removeIf(q -> q.getLifeCounter() <= 0);
  }

  public List<VisualisedMessage> getCurrent() {
    return items;
  }
}
