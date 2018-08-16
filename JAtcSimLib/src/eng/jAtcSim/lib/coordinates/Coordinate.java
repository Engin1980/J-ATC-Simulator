/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.coordinates;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Marek
 */
public final class Coordinate {

  private final CoordinateValue latitude;
  private final CoordinateValue longitude;

  public static Coordinate parseNew(String value) {
    Coordinate ret;
    final String regex = "^(N|S)? ?(-?\\d+(?:\\.\\d+)?)(?: +?(\\d+(?:\\.\\d+)?))?(?: +?(\\d+(?:\\.\\d+)?))?,? (?: ?(N|S|E|W))? ?(-?\\d+(?:\\.\\d+)?)(?: +?(\\d+(?:\\.\\d+)?))?(?: +?(\\d+(?:\\.\\d+)?))?(?: (E|W))?";
    final Pattern pattern = Pattern.compile(regex);
    final Matcher matcher = pattern.matcher(value);

    if (matcher.find()) {
      double lat = decodeValue(matcher, 1, 5, 2, 3, 4);
      double lng = decodeValue(matcher, 5, 9, 6, 7, 8);
      ret = new Coordinate(lat, lng);
    } else
      throw new IllegalArgumentException("Unable to parse " + value + " into Coordinate.");

    return ret;
  }

  private static double decodeValue(Matcher matcher,
                                    int flagIndexA, int flagIndexB, int degreeIndex, int minuteIndex, int secondIndex) {
    double ret;

    String fsA = matcher.group(flagIndexA);
    String fsB = matcher.group(flagIndexB);
    String ds = matcher.group(degreeIndex);
    String ms = matcher.group(minuteIndex);
    String ss = matcher.group(secondIndex);


    boolean isNeg = (fsA != null && (fsA.equals("S") || fsA.equals("W")))
            || (fsB != null && (fsB.equals("S") || fsB.equals("W")));
    double d;
    double m;
    double s;

    d = Double.parseDouble(ds);
    if (ms != null && ms.length() > 0)
      m = Double.parseDouble(ms);
    else
      m = 0;
    if (ss != null && ss.length() > 0)
      s = Double.parseDouble(ss);
    else
      s = 0;

    ret = d + m / 60d + s / 3600d;

    if (isNeg)
      ret = -ret;

    return ret;
  }

  public Coordinate(int aDegrees, int aMinutes, double aSeconds, boolean isSouth,
                    int bDegrees, int bMinutes, double bSeconds, boolean isWest) {
    this(
        new CoordinateValue(aDegrees, aMinutes, aSeconds, isSouth),
        new CoordinateValue(bDegrees, bMinutes, bSeconds, isWest));
  }

  public Coordinate(int aDegrees, double aMinutesSeconds, boolean isSouth,
                    int bDegrees, double bMinutesSeconds, boolean isWest) {
    this(
        new CoordinateValue(aDegrees, aMinutesSeconds, isSouth),
        new CoordinateValue(bDegrees, bMinutesSeconds, isWest));
  }

  private Coordinate() {
    this(Double.MIN_VALUE, Double.MIN_VALUE);
  }

  public Coordinate(double lat, double lon) {
    this(
        new CoordinateValue(lat), new CoordinateValue(lon));
  }

  public Coordinate(CoordinateValue lat, CoordinateValue lon) {
    this.latitude = lat;
    this.longitude = lon;
  }

  public Coordinate add(Coordinate other) {
    return add(other.getLatitude().get(), other.getLongitude().get());
  }

  public Coordinate add(double lat, double lon) {
    CoordinateValue clat = getLatitude();
    CoordinateValue clon = getLongitude();

    clat = clat.add(lat);
    clon = clon.add(lon);
    return new Coordinate(clat, clon);
  }

  public CoordinateValue getLatitude() {
    return latitude;
  }

  public CoordinateValue getLongitude() {
    return longitude;
  }

  public boolean isSame(Coordinate other) {
    return this.equals(other);
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 29 * hash + Objects.hashCode(this.latitude);
    hash = 29 * hash + Objects.hashCode(this.longitude);
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
    final Coordinate other = (Coordinate) obj;
    if (!Objects.equals(this.latitude, other.latitude)) {
      return false;
    }
    if (!Objects.equals(this.longitude, other.longitude)) {
      return false;
    }
    return true;
  }

  @Override
  @SuppressWarnings("CloneDoesntCallSuperClone")
  public Coordinate clone() {
    return new Coordinate(
        this.latitude.clone(),
        this.longitude.clone());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    if (this.latitude.get() > 0) {
      sb.append("N");
    } else {
      sb.append("S");
    }
    sb.append(this.latitude.toString(false));
    sb.append(" ");
    if (this.longitude.get() > 0) {
      sb.append("E");
    } else {
      sb.append("W");
    }
    sb.append(this.longitude.toString(false));
    return sb.toString();
  }

  public Coordinate negate() {
    return new Coordinate(-this.latitude.get(), -this.longitude.get());
  }
}
