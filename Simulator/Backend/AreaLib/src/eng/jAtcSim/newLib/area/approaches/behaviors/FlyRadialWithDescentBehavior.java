package eng.jAtcSim.newLib.area.approaches.behaviors;

import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Headings;

public class FlyRadialWithDescentBehavior extends FlyRadialBehavior {

  public static FlyRadialWithDescentBehavior create(
      Coordinate coordinate, int radial, double declination, int altitudeOverCoordinate,
      double slope) {
    return new FlyRadialWithDescentBehavior(coordinate, Headings.add(radial, declination), coordinate, altitudeOverCoordinate, slope);
  }

  private final Coordinate altitudeFixCoordinate;
  private final int altitudeFixValue;
  private final double slope;

  private FlyRadialWithDescentBehavior(Coordinate coordinate, double inboundRadialWithDeclination, Coordinate altitudeFixCoordinate, int altitudeFixValue, double slope) {
    super(coordinate, inboundRadialWithDeclination);
    this.altitudeFixCoordinate = altitudeFixCoordinate;
    this.altitudeFixValue = altitudeFixValue;
    this.slope = slope;
  }

  public Coordinate getAltitudeFixCoordinate() {
    return altitudeFixCoordinate;
  }

  public int getAltitudeFixValue() {
    return altitudeFixValue;
  }

  public double getSlope() {
    return slope;
  }
}
