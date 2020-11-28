package eng.jAtcSim.newLib.shared.time;

import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.utilites.NumberUtils;
import eng.eSystem.validation.EAssert;
import eng.newXmlUtils.annotations.XmlConstructor;

import java.time.LocalTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class ETimeStamp implements ITime, ITimeComparable<ETimeStamp> {

  public static ETimeStamp parse(String s) {
    ETimeStamp ret;
    Pattern p = Pattern.compile("(\\d{1,2}):(\\d{2}):(\\d{2})");
    Matcher m = p.matcher(s);
    if (m.find() == false)
      throw new EApplicationException(sf("Failed to parse ETimeStamp from '%s'.", s));
    else {
      String tmp;
      tmp = m.group(1);
      int hour = Integer.parseInt(tmp);
      tmp = m.group(2);
      int min = Integer.parseInt(tmp);
      tmp = m.group(3);
      int sec = Integer.parseInt(tmp);
      EAssert.isTrue(NumberUtils.isBetweenOrEqual(0, hour, 23));
      EAssert.isTrue(NumberUtils.isBetweenOrEqual(0, min, 59));
      EAssert.isTrue(NumberUtils.isBetweenOrEqual(0, sec, 59));

      ret = new ETimeStamp(hour, min, sec);
    }
    return ret;
  }
  private final int value;

  @XmlConstructor
  private ETimeStamp() {
    this.value = 0;
  }

  public ETimeStamp(int value) {
    EAssert.Argument.isTrue(value >= 0, sf("Number of seconds must be non-negative (is %d).", value));
    EAssert.Argument.isTrue(value < ITime.SECONDS_PER_DAY, "Number of seconds must be less than one day.");
    this.value = value;
  }

  public ETimeStamp(int hours, int minutes, int seconds) {
    this(hours * ITime.SECONDS_PER_HOUR + minutes * ITime.SECONDS_PER_MINUTE + seconds);
  }

  public ETimeStamp(LocalTime time) {
    this(time.getHour(), time.getMinute(), time.getSecond());
  }

  @Override
  public String format() {
    return this.toTimeString();
  }

  @Override
  public int getValue() {
    return this.value;
  }

  @Override
  public String toString() {
    return toTimeString();
  }
}
