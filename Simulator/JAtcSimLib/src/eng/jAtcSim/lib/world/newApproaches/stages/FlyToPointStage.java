package eng.jAtcSim.lib.world.newApproaches.stages;

import eng.eSystem.geo.Coordinate;

public class FlyToPointStage implements IApproachStage {
  private Coordinate coordinate;

  public Coordinate getCoordinate() {
    return coordinate;
  }
}
