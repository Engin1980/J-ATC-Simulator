/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.coordinates;

import eng.eSystem.exceptions.EApplicationException;

import java.awt.geom.RoundRectangle2D;
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
    final String regex = "^(N|S)? ?(-?\\d+(?:\\.\\d+)?)(?: +?(\\d+(?:\\.\\d+)?))?(?: +?(\\d+(?:\\.\\d+)?))?,? (E|W)? ?(-?\\d+(?:\\.\\d+)?)(?: +?(\\d+(?:\\.\\d+)?))?(?: +?(\\d+(?:\\.\\d+)?))?";
    final Pattern pattern = Pattern.compile(regex);
    final Matcher matcher = pattern.matcher(value);

    if (matcher.find()) {
      double lat = decodeValue(matcher, 1, 2, 3, 4);
      double lng = decodeValue(matcher, 5, 6, 7, 8);
      ret = new Coordinate(lat, lng);
    } else
      throw new IllegalArgumentException("Unable to parse " + value + " into Coordinate.");

    return ret;
  }

  @Deprecated
  public static Coordinate parseOld(String value) {
    Coordinate ret = tryParseA(value);
    if (ret == null) {
      ret = tryParseB(value);
    }
    if (ret == null) {
      ret = tryParseC(value);
    }
    if (ret == null) {
      ret = tryParseD(value);
    }
    if (ret == null) {
      ret = tryParseE(value);
    }

    if (ret == null) {
      throw new IllegalArgumentException("Unable to parseOld " + value + " into Coordinate.");
    }

    return ret;
  }

  private static double decodeValue(Matcher matcher,
                                    int flagIndex, int degreeIndex, int minuteIndex, int secondIndex) {
    double ret;

    String fs = matcher.group(flagIndex);
    String ds = matcher.group(degreeIndex);
    String ms = matcher.group(minuteIndex);
    String ss = matcher.group(secondIndex);


    boolean isNeg = fs != null && (fs.equals("S") || fs.equals("W"));
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

  /**
   * Parses 503113.59N 0012000.23W to coordinate.
   *
   * @param value
   * @return
   */
  private static Coordinate tryParseC(String value) {
    Coordinate ret = null;

    String patternString = "(\\d{2})(\\d{2})(\\d{2}(\\.\\d+)?)([NS]) (\\d{3})(\\d{2})(\\d{2}(\\.\\d+)?)([EW])";
    Pattern p = Pattern.compile(patternString);
    Matcher m = p.matcher(value);
    if (m.find()) {
      try {
        int latH = Integer.parseInt(m.group(1));
        int latM = Integer.parseInt(m.group(2));
        double latS = Double.parseDouble(m.group(3));
        int lonH = Integer.parseInt(m.group(6));
        int lonM = Integer.parseInt(m.group(7));
        double lonS = Double.parseDouble(m.group(8));
        ret = new Coordinate(
            new CoordinateValue(latH, latM, latS, m.group(5).equals("S")),
            new CoordinateValue(lonH, lonM, lonS, m.group(10).equals("W"))
        );
      } catch (Exception ex) {
        ret = null;
      }
    }

    return ret;
  }

  /**
   * Parses 50.257243 7.07292 to coordinate
   *
   * @param value
   * @return
   */
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

  /**
   * Parses 40.20324, 10.05343 to coordinate
   *
   * @param value
   * @return
   */
  private static Coordinate tryParseE(String value) {
    Coordinate ret = null;

    String patternString = "(-?\\d+(\\.\\d+)?), (-?\\d+(\\.\\d+)?)";
    Pattern p = Pattern.compile(patternString);
    Matcher m = p.matcher(value);
    if (m.find()) {
      String latS = m.group(1);
      String lngS = m.group(3);

      double lat;
      double lng;

      try {
        lat = Double.parseDouble(latS);
        lng = Double.parseDouble(lngS);
      } catch (Exception ex) {
        throw new EApplicationException("Failed to convert " + latS + "/" + lngS + " to doubles.");
      }

      ret = new Coordinate(
          new CoordinateValue(lat),
          new CoordinateValue(lng)
      );
    }

    return ret;
  }

  /**
   * Parses N40 30.203 W20 2.502 to coordinate.
   *
   * @param value
   * @return
   */
  private static Coordinate tryParseD(String value) {
    Coordinate ret = null;

    String patternString = "(N|S)(\\d+) (\\d{1,2}(\\.\\d+)) (E|W)(\\d+) (\\d{1,2}(\\.\\d+))";
    Pattern p = Pattern.compile(patternString);
    Matcher m = p.matcher(value);
    if (m.find()) {
      char latA = m.group(1).charAt(0);
      int latDeg = Integer.parseInt(m.group(2));
      double latMins = Double.parseDouble(m.group(3));


      char lngtA = m.group(5).charAt(0);
      int lngDeg = Integer.parseInt(m.group(6));
      double lngMins = Double.parseDouble(m.group(7));

      double lat = latDeg + latMins / 60d;
      if (latA == 'S')
        lat = -lat;

      double lng = lngDeg + lngMins / 60d;
      if (lngtA == 'W')
        lng = -lng;

      ret = new Coordinate(
          new CoordinateValue(lat),
          new CoordinateValue(lng)
      );
    }

    return ret;
  }

  /**
   * Parses 50 23 29.24 N 1 12 29.52 E to coordinate.
   *
   * @param value
   * @return
   */
  private static Coordinate tryParseB(String value) {
    Coordinate ret = null;

    String patternString = "(\\d{2}) (\\d{2}) (\\d{2}(?:\\.\\d{2,4})?) ([NS]) (\\d{1,3}) (\\d{2}) (\\d{2}(?:\\.\\d{2,4})?) ([EW])";
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
