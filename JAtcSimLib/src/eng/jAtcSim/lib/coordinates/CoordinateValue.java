/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.coordinates;

import eng.eSystem.EMath;
import eng.jAtcSim.lib.global.Global;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * @author Marek
 */
public final class CoordinateValue {

  private final boolean neg;
  private final double value;

  private CoordinateValue() {
    this(Double.MIN_VALUE);
  }

  public CoordinateValue(double value) {
    this.neg = value < 0;
    this.value = Math.abs(value);
  }

  public CoordinateValue(int degrees, int minutes, double seconds, boolean isSouthOrWest) {
    this(
        toNumber(
            degrees,
            minutes,
            seconds,
            isSouthOrWest));
  }

  public CoordinateValue(int degrees, double minutesSeconds, boolean isSouthOrWest) {
    this(toNumber(degrees, minutesSeconds, isSouthOrWest));
  }

  private static double toNumber(int degrees, int minutes, double seconds, boolean isNegative) {
    if (degrees < 0 || minutes < 0 || seconds < 0)
      throw new IllegalArgumentException("All numeric parameters must be positive values.");
    double ret;
    if (isNegative)
      ret = -degrees - minutes / 60d - seconds / 3600d;
    else
      ret = degrees + minutes / 60d + seconds / 3600d;
    return ret;
  }

  private static double toNumber(int degrees, double minutesSeconds, boolean isNegative) {
    if (degrees < 0 || minutesSeconds < 0)
      throw new IllegalArgumentException("All numeric parameters must be positive values.");
    double ret;
    if (!isNegative)
      ret = degrees + minutesSeconds / 60d;
    else
      ret = -degrees - minutesSeconds / 60d;
    return ret;
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
    return EMath.roundToZero(value);
  }

  /**
   * Returns value in total decimal degrees, never NEGATIVE
   *
   * @return Positive total degrees deicmal value
   */
  public double getTotalDegrees() {
    double ret = neg ? -value : value;
    return ret;
  }

  public int getMinutes() {
    double pom = value - getDegrees();
    pom = pom * 60;
    int ret = EMath.roundToZero(pom);
    return ret;
  }

  public double getSeconds() {
    double ret = value - getDegrees();
    ret = ret * 60 - getMinutes();
    ret = ret * 60;
    return ret;
  }

  public String toString(boolean useSign) {
    String ret;
    if (Global.COORDINATE_LONG) {
      ret = toDegreeString(useSign);
    } else {
      ret = toDecimalString(useSign);
    }
    return ret;
  }

  public String toDegreeString(boolean useSign) {
    String ret;
    NumberFormat nfa = new DecimalFormat("00");
    NumberFormat nfb = new DecimalFormat("00.00");

    StringBuilder sb = new StringBuilder();
    if (useSign && neg) {
      sb.append("-");
    }
    sb.append(nfa.format(Math.abs(getDegrees())));
    sb.append("°");
    sb.append(nfa.format(Math.abs(getMinutes())));
    sb.append("'");
    sb.append(nfb.format(Math.abs(getSeconds())));
    sb.append("\"");
    ret = sb.toString();
    return ret;
  }

  public String toDecimalString(boolean useSign) {
    NumberFormat nf = new DecimalFormat("00.00000");
    String ret = nf.format(Math.abs(this.value));
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

  @Override
  @SuppressWarnings("CloneDoesntCallSuperClone")
  public CoordinateValue clone() {
    return new CoordinateValue(value);
  }

  @Override
  public String toString() {
    return toString(true);
  }

}
