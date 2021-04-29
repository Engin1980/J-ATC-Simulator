package eng.jAtcSim.newLib.newStats;

public class ElapsedSecondCounter {
  private int value = 0;

  public int get() {
    return value;
  }

  public void inc() {
    value++;
  }
}
