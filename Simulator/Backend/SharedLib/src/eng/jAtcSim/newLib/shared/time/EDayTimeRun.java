package eng.jAtcSim.newLib.shared.time;

public class EDayTimeRun extends ETime implements ITimeComparable<EDayTimeStamp>, IDayGetter, ITimeGetter  {
  public EDayTimeRun(int value) {
    super(value);
  }

  public void addSecond(){
    super.addSecond();
  }
}
