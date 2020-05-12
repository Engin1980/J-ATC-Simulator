package eng.jAtcSim.abstractRadar.support;

import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;

public class InfoLine {
  public static String toIntegerMinutes(double value) {
    int tmp = (int) (value / 60);
    return Integer.toString(tmp);
  }

  public static String toIntegerSeconds(double value) {
    double tmp = value % 60;
    return String.format("%02.0f", tmp);
  }

  public final Coordinate from;
  public final Coordinate to;
  public final int heading;
  public final double distanceInNm;
  public final double seconds200;
  public final double seconds250;
  public final double seconds280;
  public final double secondsSpeed;
  public final boolean isRelativeSpeedUsed;

  public InfoLine(Coordinate from, Coordinate to, Double refSpeed) {
    this.from = from;
    this.to = to;
    this.distanceInNm = Coordinates.getDistanceInNM(from, to);
    this.heading = (int) Coordinates.getBearing(from, to);
    if (refSpeed == null) {
      this.seconds200 = this.distanceInNm / 200d * 3600d;
      this.seconds250 = this.distanceInNm / 250d * 3600d;
      this.seconds280 = this.distanceInNm / 280d * 3600d;
      this.secondsSpeed = 0;
      this.isRelativeSpeedUsed = false;
    } else {
      this.secondsSpeed = this.distanceInNm / refSpeed * 3600d;
      this.seconds200 = 0;
      this.seconds250 = 0;
      this.seconds280 = 0;
      this.isRelativeSpeedUsed = true;
    }
  }
}