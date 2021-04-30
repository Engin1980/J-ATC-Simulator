package eng.jAtcSim.newLib.newStats.values;

public class ElapsedSecondCounter {
  private int value = 0;

  public int get() {
    return value;
  }

  public void inc() {
    value++;
  }
}
