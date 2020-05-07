package eng.jAtcSim.abstractRadar.global.events;

import eng.eSystem.geo.Coordinate;

public class WithCoordinateEventArg {
  public final Coordinate coordinate;

  public WithCoordinateEventArg(Coordinate coordinate) {
    this.coordinate = coordinate;
  }
}
