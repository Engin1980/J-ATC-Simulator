package eng.jAtcSim.newLib.traffic.models.base;

import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.utilites.NumberUtils;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.traffic.movementTemplating.MovementTemplate;

import static eng.jAtcSim.newLib.shared.SharedFactory.getRnd;

public abstract class DayGeneratedTrafficModel {
  public abstract IReadOnlyList<MovementTemplate> generateMovementsForOneDay();
}

