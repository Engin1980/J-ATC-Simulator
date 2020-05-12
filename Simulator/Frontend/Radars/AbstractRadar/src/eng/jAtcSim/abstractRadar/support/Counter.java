package eng.jAtcSim.abstractRadar.support;

public class Counter {
  private final int maximum;
  private int value;

  public Counter(int maximum) {
    assert maximum > 0;
    this.maximum = maximum;
    this.value = 0;
  }

  public boolean increase() {
    value++;
    if (value == maximum) {
      value = 0;
      return true;
    } else
      return false;
  }
}
