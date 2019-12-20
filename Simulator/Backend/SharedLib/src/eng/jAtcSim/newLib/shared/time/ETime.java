package eng.jAtcSim.newLib.shared.time;

public abstract class ETime implements ITime {
  private int value;

  protected ETime(int value) {
    this.value = value;
  }

  @Override
  public int getValue() {
    return this.value;
  }

  protected void addSecond() {
    this.value++;
  }
}
