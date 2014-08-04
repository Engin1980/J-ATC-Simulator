/*
 * http://www.movable-type.co.uk/scripts/latlong.html
 */
package jatcsimlib.coordinates;

import jatcsimlib.global.UnitProvider;
import jatcsimlib.coordinates.Coordinate;

/**
 *
 * @author Marek
 */
public final class Coordinates {

  private final static double R = 6371;

  /**
   * Returns new Coordinate based on initial point, distance
   * and heading.
   *
   * @param coordinate Original point
   * @param heading Heading
   * @param distanceInNM Distance in nauctional miles
   * @return New point
   */
  public static Coordinate getCoordinate(Coordinate coordinate, int heading, double distanceInNM) {
    double lat = toRadians(coordinate.getLatitude().get());
    double lon = toRadians(coordinate.getLongitude().get());
    double bear = toRadians(heading);
    double d = UnitProvider.nmToKm(distanceInNM);
    double f = d / R;

    double rLat = Math.asin(
        Math.sin(lat) * Math.cos(f) + Math.cos(lat) * Math.sin(f) * Math.cos(bear));
    double rLon
        = lon + Math.atan2(Math.sin(bear) * Math.sin(f) * Math.cos(lat), Math.cos(f) - Math.sin(lat) * Math.sin(rLat));

    rLat = toDegrees(rLat);
    rLon = toDegrees(rLon);

    Coordinate ret = new Coordinate(rLat, rLon);
    return ret;
  }

  private Coordinates() {
  }

  /**
   * Returns distance between two points
   *
   * @param a First point
   * @param b Second point
   * @return Distance in nauctional miles
   */
  public static double getDistanceInNM(Coordinate a, Coordinate b) {
    double s1 = a.getLatitude().get();
    s1 = toRadians(s1);
    double s2 = b.getLatitude().get();
    s2 = toRadians(s2);
    double ds = b.getLatitude().get() - a.getLatitude().get();
    ds = toRadians(ds);
    double dl = b.getLongitude().get() - a.getLongitude().get();
    dl = toRadians(dl);

    double aa = Math.sin(ds / 2) * Math.sin(ds / 2)
        + Math.cos(s1) * Math.cos(s2)
        * Math.sin(dl / 2) * Math.sin(dl / 2);
    double cc = 2 * Math.atan2(Math.sqrt(aa), Math.sqrt(1 - aa));
    double ret = R * cc;
    ret = UnitProvider.kmToNM(ret);

    return ret;
  }

  /**
   * Returns initial bearing
   *
   * @param from Initial point
   * @param to Target point
   * @return Bearing in degrees
   */
  public static double getBearing(Coordinate from, Coordinate to) {
    double dLon = to.getLongitude().get() - from.getLongitude().get();
    dLon = toRadians(dLon);

    double lat2 = toRadians(to.getLatitude().get());
    double lat1 = toRadians(from.getLatitude().get());

    double x = Math.sin(dLon) * Math.cos(lat2);
    double y = Math.cos(lat1) * Math.sin(lat2)
        - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);
    double ret = Math.atan2(x, y);
    ret = toDegrees(ret);
    ret = (ret + 360) % 360;
    return ret;
  }

  private static double toRadians(double degrees) {
    return degrees * Math.PI / 180;
  }

  private static double toDegrees(double radians) {
    return radians * 180 / Math.PI;
  }
}
