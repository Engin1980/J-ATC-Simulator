package eng.jAtcSim.newLib.traffic.models;

import eng.eSystem.collections.IReadOnlyList;
import eng.jAtcSim.newLib.traffic.models.base.ITrafficModel;
import eng.jAtcSim.newLib.traffic.movementTemplating.MovementTemplate;

public class FlightListTrafficModel implements ITrafficModel {
  @Override
  public IReadOnlyList<MovementTemplate> generateMovementsForOneDay() {
    return null;
  }
}
