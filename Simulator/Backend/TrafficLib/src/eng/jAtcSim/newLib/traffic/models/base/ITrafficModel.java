package eng.jAtcSim.newLib.traffic.models.base;

import eng.eSystem.collections.IReadOnlyList;
import eng.jAtcSim.newLib.traffic.movementTemplating.MovementTemplate;

public interface ITrafficModel {
  IReadOnlyList<MovementTemplate> generateMovementsForOneDay();
}
