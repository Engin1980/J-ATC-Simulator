package eng.jAtcSim.newLib.area.approaches.behaviors;

import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Headings;
import eng.jAtcSim.newLib.shared.PostContracts;
import eng.newXmlUtils.annotations.XmlConstructor;

public class FlyRadialWithDescentBehavior extends FlyRadialBehavior {
  private String tag;

  public FlyRadialWithDescentBehavior withTag(String value){
    this.tag = value;
    return this;
  }

  public static FlyRadialWithDescentBehavior create(
      Coordinate coordinate, int radial, double declination, int altitudeOverCoordinate,
      double slope) {
    return new FlyRadialWithDescentBehavior(coordinate, Headings.add(radial, declination), coordinate, altitudeOverCoordinate, slope);
  }

  private final Coordinate altitudeFixCoordinate;
  private final int altitudeFixValue;
  private final double slope;

  @XmlConstructor
  private FlyRadialWithDescentBehavior(){
    super();
    this.altitudeFixCoordinate= null;
    this.altitudeFixValue = 0;
    this.slope = 0;

    PostContracts.register(this, () -> this.altitudeFixCoordinate != null);
  }

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
