package eng.jAtcSim.newLib.stats.properties;

import eng.eSystem.validation.EAssert;

public class CounterProperty {
  private int count = 0;

  public void add() {
    count++;
  }

  public void add(int count) {
    EAssert.isTrue(count >= 0);
    this.count += count;
  }

  public int getCount() {
    return count;
  }
}
