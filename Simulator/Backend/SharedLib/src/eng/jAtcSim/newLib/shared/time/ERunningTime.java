package eng.jAtcSim.newLib.shared.time;

public class ERunningTime extends ETime {
  public ERunningTime(int value) {
    super(value);
  }

  public void increaseSecond() {
    super.addOneSecond();
  }
}
