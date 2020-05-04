package eng.jAtcSim.newLib.traffic;

import eng.eSystem.collections.IReadOnlyList;
import eng.jAtcSim.newLib.traffic.movementTemplating.MovementTemplate;

public interface ITrafficModel {
  IReadOnlyList<MovementTemplate> generateMovementsForOneDay();
}
