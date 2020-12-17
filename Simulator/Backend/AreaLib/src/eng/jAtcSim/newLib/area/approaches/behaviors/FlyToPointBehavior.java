package eng.jAtcSim.newLib.area.approaches.behaviors;

import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.shared.PostContracts;
import eng.newXmlUtils.annotations.XmlConstructor;

public class FlyToPointBehavior implements IApproachBehavior {
  public static FlyToPointBehavior create(Coordinate coordinate) {
    return new FlyToPointBehavior(coordinate);
  }
  private final Coordinate coordinate;

  @XmlConstructor
  protected FlyToPointBehavior() {
    this.coordinate = null;
    PostContracts.register(this, () -> coordinate != null);
  }

  protected FlyToPointBehavior(Coordinate coordinate) {
    this.coordinate = coordinate;
  }

  public Coordinate getCoordinate() {
    return coordinate;
  }
}
