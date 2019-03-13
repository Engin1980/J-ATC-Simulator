package eng.jAtcSim.lib.world;

import eng.eSystem.geo.Coordinate;

public class InitialPosition {
  public Coordinate coordinate;
  public double range;

  public InitialPosition(Coordinate coordinate, double range) {
    this.coordinate = coordinate;
    this.range = range;
  }
}
