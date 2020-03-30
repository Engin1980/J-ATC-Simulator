package eng.jAtcSim.newLib.area.approaches.behaviors;

import eng.eSystem.collections.*;
import eng.eSystem.geo.Coordinate;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class FlyRadialWithDescentBehavior extends FlyRadialBehavior {

  private final Coordinate altitudeFixCoordinate;
  private final int altitudeFixValue;
  private final double slope;

  public FlyRadialWithDescentBehavior(Coordinate radialCoordinate, int inboundRadial,
                                      Coordinate altitudeFixCoordinate, int altitudeFixValue, double slope) {
    super(radialCoordinate, inboundRadial);
    this.altitudeFixCoordinate = altitudeFixCoordinate;
    this.altitudeFixValue = altitudeFixValue;
    this.slope = slope;
  }

  public FlyRadialWithDescentBehavior(Coordinate coordinate, int inboundRadial, int altitudeOverCoordinate, double slope) {
    super(coordinate, inboundRadial);
    this.altitudeFixCoordinate = coordinate;
    this.altitudeFixValue = altitudeOverCoordinate;
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
