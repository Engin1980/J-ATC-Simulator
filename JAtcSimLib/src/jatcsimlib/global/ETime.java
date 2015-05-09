/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.global;

import java.util.Calendar;

/**
 *
 * @author Marek
 */
public class ETime implements Comparable<ETime> {

  private int value = 0;

  public ETime(int value) {
    this.value = value;
  }

  public ETime(int hour, int minute, int second) {
    set(hour, minute, second);
  }

  public ETime(Calendar c) {
    int h = c.get(Calendar.HOUR_OF_DAY);
    int m = c.get(Calendar.MINUTE);
    int s = c.get(Calendar.SECOND);

    set(h, m, s);
  }

  public final void set(int hours, int minutes, int seconds) {
    this.value = hours * 60 * 60 + minutes * 60 + seconds;
  }

  public int getSeconds() {
    return value % 60;
  }
  
  public int getTotalSeconds(){
    return value;
  }

  public int getMinutes() {
    return ((int) (Math.floor(value / 60))) % 60;
  }

  public int getHours() {
    return ((int) (Math.floor(value / 60 / 60)));
  }

  @Override
  public String toString() {
    return String.format("%02d:%02d:%02d", getHours(), getMinutes(), getSeconds());
  }

  public ETime addSeconds(int amount) {
    return new ETime(value + amount);
  }

  @Override
  public ETime clone() {
    return new ETime(this.value);
  }

  @Override
  public int compareTo(ETime o) {
    return Integer.compare(this.value, o.value);
  }

  public boolean isBefore(ETime otherTime) {
    return this.value < otherTime.value;
  }

  public boolean isAfter(ETime otherTime) {
    return this.value > otherTime.value;
  }

  public boolean isBeforeOrEq(ETime otherTime) {
    return isBefore(otherTime) || this.value == otherTime.value;
  }

  public boolean isAfterOrEq(ETime otherTime) {
    return isAfter(otherTime) || this.value == otherTime.value;
  }

  public boolean isBetween(ETime before, ETime after) {
    return this.isAfterOrEq(before) && this.isBeforeOrEq(after);
  }

  public boolean isBetweenStrictly(ETime before, ETime after) {
    return this.isAfter(before) && this.isBefore(after);
  }

  public void increaseSecond() {
    this.value++;
  }
}
