package eng.jAtcSim.newLib.area.newStats.properties;

import eng.eSystem.validation.Validator;

public class CounterProperty {
  private int count = 0;

  public void add() {
    count++;
  }

  public void add(int count) {
    Validator.check(count >= 0);
    this.count += count;
  }

  public int getCount() {
    return count;
  }
}
