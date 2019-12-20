package eng.jAtcSim.newLib.shared.time;

public interface IDayGetter extends ITime {

  default int getDays(){
    return getValue() / ITimeGetter.SECONDS_PER_DAY;
  }

}
