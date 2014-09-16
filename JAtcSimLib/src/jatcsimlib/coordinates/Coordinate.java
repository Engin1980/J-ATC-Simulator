/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.coordinates;

import jatcsimlib.exceptions.ERuntimeException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Marek
 */
public final class Coordinate {

  public static Coordinate parse(String value) {
    Coordinate ret = tryParseA(value);
    if (ret == null) {
      ret = tryParseB(value);
    }

    if (ret == null) {
      throw new ERuntimeException("Unable to parse " + value + " into Coordinate.");
    }

    return ret;
  }

  private static Coordinate tryParseA(String value) {
    Coordinate ret = null;

    String patternString = "^(-?\\d+(\\.\\d+)?) (-?\\d+(\\.\\d+)?)$";
    Pattern p = Pattern.compile(patternString);
    Matcher m = p.matcher(value);
    if (m.find()) {
      double a = Double.parseDouble(m.group(1));
      double o = Double.parseDouble(m.group(3));
      ret = new Coordinate(
          new CoordinateValue(a),
          new CoordinateValue(o)
      );
    }

    return ret;
  }

  private static Coordinate tryParseB(String value) {
    Coordinate ret = null;

    String patternString = "(\\d{2}) (\\d{2}) (\\d{2}\\.\\d{2}) ([NS]) (\\d{3}) (\\d{2}) (\\d{2}\\.\\d{2}) ([EW])";
    Pattern p = Pattern.compile(patternString);
    Matcher m = p.matcher(value);
    if (m.find()) {
      int aa = Integer.parseInt(m.group(1));
      int ab = Integer.parseInt(m.group(2));
      double ac = Double.parseDouble(m.group(3));
      String ad = m.group(4);
      int oa = Integer.parseInt(m.group(5));
      int ob = Integer.parseInt(m.group(6));
      double oc = Double.parseDouble(m.group(7));
      String od = m.group(8);

      ret = new Coordinate(
          new CoordinateValue(aa, ab, ac, ad.equals("S")),
          new CoordinateValue(oa, ob, oc, od.equals("W"))
      );
    }

    return ret;
  }

  private final CoordinateValue latitude;
  private final CoordinateValue longitude;

  public Coordinate(int aDegrees, int aMinutes, double aSeconds,
      int bDegrees, int bMinutes, double bSeconds) {
    this(
        new CoordinateValue(aDegrees, aMinutes, aSeconds),
        new CoordinateValue(bDegrees, bMinutes, bSeconds));
  }

  public Coordinate(int aDegrees, double aMinutesSeconds,
      int bDegrees, double bMinutesSeconds) {
    this(
        new CoordinateValue(aDegrees, aMinutesSeconds),
        new CoordinateValue(bDegrees, bMinutesSeconds));
  }

  public Coordinate(double lat, double lon) {
    this(
      new CoordinateValue(lat), new CoordinateValue(lon));
  }

  public Coordinate(CoordinateValue lat, CoordinateValue lon) {
    this.latitude = lat;
    this.longitude = lon;
  }

  public Coordinate add(Coordinate other){
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

  @Override
  @SuppressWarnings("CloneDoesntCallSuperClone")
  public Coordinate clone() {
    return new Coordinate(
        this.latitude.clone(),
        this.longitude.clone());
  }

  public boolean isSame(Coordinate other) {
    return this.equals(other);
  }

  @Override
  public int hashCode() {
    int hash =  5;
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
