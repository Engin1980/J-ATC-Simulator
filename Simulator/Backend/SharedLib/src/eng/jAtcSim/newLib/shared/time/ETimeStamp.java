/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.newLib.shared.time;

import java.time.LocalTime;
import java.util.Objects;

/**
 *
 * @author Marek
 */
public class ETimeStamp extends ETime {

  public ETimeStamp(int value) {
    super(value);
  }

  public ETimeStamp(int hour, int minute, int second) {
    super(hour, minute, second);
  }

  public ETimeStamp(int day, int hour, int minute, int second) {
    super(day, hour, minute, second);
  }

  public ETimeStamp(LocalTime localTime) {
    super(localTime);
  }

  @Override
  public ETimeStamp clone() {
    return new ETimeStamp(this.getValue());
  }
}
