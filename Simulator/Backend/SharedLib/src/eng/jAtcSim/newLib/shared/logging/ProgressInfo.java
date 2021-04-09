package eng.jAtcSim.newLib.shared.logging;

import eng.eSystem.events.EventAnonymous;
import eng.eSystem.events.EventAnonymousSimple;

public class ProgressInfo {

  public static class ProgressMessage {
    public final int value;
    public final String text;

    private ProgressMessage(int value, String text) {
      this.value = value;
      this.text = text;
    }
  }

  private String lastMessage;
  private int lastValue;

  public final EventAnonymous<Integer> onInitMaximum = new EventAnonymous<>();
  public final EventAnonymous<ProgressMessage> onProgress = new EventAnonymous<>();

  public final EventAnonymousSimple onDone = new EventAnonymousSimple();

  public void done() {
    this.onDone.raise();
  }

  public void increase() {
    lastValue++;
    raiseProgress();
  }

  public void increase(String message) {
    lastMessage = message;
    increase();
  }

  public void init(int maximum) {
    this.lastMessage = "";
    this.lastValue = 0;
    this.onInitMaximum.raise(maximum);
    raiseProgress();
  }

  public void message(String message) {
    this.lastMessage = message;
    raiseProgress();
  }

  public void set(int value, String message) {
    lastMessage = message;
    set(value);
  }

  public void set(int value) {
    this.lastValue = value;
    raiseProgress();
  }

  private void raiseProgress() {
    this.onProgress.raise(new ProgressMessage(lastValue, lastMessage));
  }
}
