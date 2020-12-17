package eng.jAtcSim.newLib.area.approaches.behaviors;

import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.shared.PostContracts;
import eng.newXmlUtils.annotations.XmlConstructor;

public class FlyToPointWithDescentBehavior extends FlyToPointBehavior {

  private final int altitudeFixValue;
  private final double slope;

  public static FlyToPointWithDescentBehavior create(Coordinate coordinate, int altitudeFixValue, double slope){
    return new FlyToPointWithDescentBehavior(coordinate, altitudeFixValue, slope);
  }

  protected FlyToPointWithDescentBehavior(Coordinate coordinate, int altitudeFixValue, double slope) {
    super(coordinate);
    this.altitudeFixValue = altitudeFixValue;
    this.slope = slope;
  }

  @XmlConstructor
  protected FlyToPointWithDescentBehavior() {
    this.altitudeFixValue = 0;
    this.slope = -1;

    PostContracts.register(this, () -> this.slope > 0);
  }

  public int getAltitudeFixValue() {
    return altitudeFixValue;
  }

  public double getSlope() {
    return slope;
  }
}
