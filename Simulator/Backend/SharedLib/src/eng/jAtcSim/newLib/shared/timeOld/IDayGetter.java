package eng.jAtcSim.newLib.shared.time;

public interface IDayGetter extends ITime, ITimeGetter {

  default int getDays(){
    return getValue() / ITimeGetter.SECONDS_PER_DAY;
  }

  default String toDayTimeString(){
    return String.format("%d.%02d:%02d:%02d", getDays(), getHours(), getMinutes(), getSeconds());
  }
}
