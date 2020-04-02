package eng.jAtcSim.newLib.area.approaches.behaviors;

import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.area.approaches.perCategoryValues.DoublePerCategoryValue;

public class FlyRadialWithDescentBehavior extends FlyRadialBehavior {

  public static FlyRadialWithDescentBehavior create(
      Coordinate coordinate, int radial, int altitudeOverCoordinate,
      double slope) {
    return new FlyRadialWithDescentBehavior(coordinate, radial, coordinate, altitudeOverCoordinate, slope);
  }

  private final Coordinate altitudeFixCoordinate;
  private final int altitudeFixValue;
  private final double slope;

  private FlyRadialWithDescentBehavior(Coordinate coordinate, int inboundRadial, Coordinate altitudeFixCoordinate, int altitudeFixValue, double slope) {
    super(coordinate, inboundRadial);
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
