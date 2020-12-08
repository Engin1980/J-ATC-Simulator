package eng.jAtcSim.newLib.area.approaches.behaviors;

import eng.eSystem.geo.Coordinate;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.PostContracts;
import eng.newXmlUtils.annotations.XmlConstructor;

public class FlyRadialBehavior implements IApproachBehavior {
  private final Coordinate coordinate;
  private final double inboundRadialWithDeclination;

  @XmlConstructor
  protected FlyRadialBehavior() {
    this.coordinate = null;
    this.inboundRadialWithDeclination = 0;

    PostContracts.register(this, () -> this.coordinate != null);
  }

  protected FlyRadialBehavior(Coordinate coordinate, double inboundRadialWithDeclination) {
    EAssert.Argument.isNotNull(coordinate, "coordinate");
    this.coordinate = coordinate;
    this.inboundRadialWithDeclination = inboundRadialWithDeclination;
  }

  public Coordinate getCoordinate() {
    return coordinate;
  }

  public double getInboundRadialWithDeclination() {
    return inboundRadialWithDeclination;
  }
}
