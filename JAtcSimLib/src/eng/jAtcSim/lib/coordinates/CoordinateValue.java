/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.coordinates;

import eng.jAtcSim.lib.global.EMath;
import eng.jAtcSim.lib.global.Global;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 *
 * @author Marek
 */
public final class CoordinateValue {

  private final boolean neg;
  private final double value;

  public CoordinateValue(double value) {
    this.neg = value < 0;
    this.value = Math.abs(value);
  }

  public CoordinateValue(int degrees, int minutes, double seconds) {
    this(degrees + minutes / 60d + seconds / 3600d);
  }

  public CoordinateValue(int degrees, int minutes, double seconds, boolean isSouthOrWest) {
    this((isSouthOrWest
        ? -(degrees + minutes / 60d + seconds / 3600d)
        : degrees + minutes / 60d + seconds / 3600d));
  }

  public CoordinateValue(int degrees, double minutesSeconds) {
    this(degrees + minutesSeconds / 60d);
  }

  public CoordinateValue add(double value) {
    double pom = get();
    pom = pom + value;
    return new CoordinateValue(pom);
  }

  /**
   * Returns value in total decimal degrees.
   *
   * @return Total decimal degrees
   */
  public double get() {
    if (neg) {
      return -this.value;
    } else {
      return this.value;
    }
  }

  public boolean isNeg() {
    return neg;
  }

  public int getDegrees() {
    return (int) EMath.down(value);
  }

  /**
   * Returns value in total decimal degrees, never NEGATIVE
   *
   * @return Positive total degrees deicmal value
   */
  public double getTotalDegrees() {
    return value;
  }

  public int getMinutes() {
    double pom = value - getDegrees();
    pom = pom * 60;
    int ret = (int) EMath.down(pom);
    return ret;
  }

  public double getSeconds() {
    double ret = value - getDegrees();
    ret = ret * 60 - getMinutes();
    ret = ret * 60;
    return ret;
  }

  @Override
  @SuppressWarnings("CloneDoesntCallSuperClone")
  public CoordinateValue clone() {
    return new CoordinateValue(value);
  }

  @Override
  public String toString() {
    return toString(true);
  }

  public String toString(boolean useSign) {
    String ret;
    if (Global.COORDINATE_LONG) {
      ret = toDegreeString();
    } else {
      ret = toDecimalString();
    }
    return ret;
  }

  public String toDegreeString() {
    String ret;
    boolean useSign = false;
    NumberFormat nfa = new DecimalFormat("00");
    NumberFormat nfb = new DecimalFormat("00.00");

    StringBuilder sb = new StringBuilder();
    if (useSign && neg) {
      sb.append("-");
    }
    sb.append(nfa.format(getDegrees()));
    sb.append("°");
    sb.append(nfa.format(getMinutes()));
    sb.append("'");
    sb.append(nfb.format(getSeconds()));
    sb.append("\"");
    ret = sb.toString();
    return ret;
  }

  public String toDecimalString() {
    boolean useSign = true;
    NumberFormat nf = new DecimalFormat("00.00000");
    String ret = nf.format(this.value);
    if (useSign && neg) {
      ret = "-" + ret;
    }
    return ret;
  }

  public boolean isSame(CoordinateValue other) {
    return this.equals(other);
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 79 * hash + (this.neg ? 1 : 0);
    hash = 79 * hash + (int) (Double.doubleToLongBits(this.value) ^ (Double.doubleToLongBits(this.value) >>> 32));
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final CoordinateValue other = (CoordinateValue) obj;
    if (this.neg != other.neg) {
      return false;
    }
    if (Double.doubleToLongBits(this.value) != Double.doubleToLongBits(other.value)) {
      return false;
    }
    return true;
  }

}
