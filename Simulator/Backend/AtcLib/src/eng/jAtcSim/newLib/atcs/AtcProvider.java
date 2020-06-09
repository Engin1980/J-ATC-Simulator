package eng.jAtcSim.newLib.atcs;

import eng.eSystem.collections.EDistinctList;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.atcs.context.AtcAcc;
import eng.jAtcSim.newLib.atcs.internal.Atc;
import eng.jAtcSim.newLib.atcs.internal.CenterAtc;
import eng.jAtcSim.newLib.atcs.internal.ComputerAtc;
import eng.jAtcSim.newLib.atcs.internal.UserAtc;
import eng.jAtcSim.newLib.atcs.internal.tower.TowerAtc;
import eng.jAtcSim.newLib.atcs.planeResponsibility.PlaneResponsibilityManager;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.enums.AtcType;

public class AtcProvider {
  private final AtcList<Atc> atcs = new AtcList<>(
      q -> q.getAtcId(), EDistinctList.Behavior.exception);
  private final AtcList<AtcId> atcIds = new AtcList<>(
      q -> q, EDistinctList.Behavior.exception);
  private final PlaneResponsibilityManager prm = new PlaneResponsibilityManager();

  public AtcProvider(Airport activeAirport) {
    for (eng.jAtcSim.newLib.area.Atc atcTemplate : activeAirport.getAtcTemplates()) {
      Atc atc = createAtc(atcTemplate);
      atcs.add(atc);
    }

    AtcAcc.setAtcListProducer(() -> atcIds);
  }

  public void adviceWeatherUpdated() {
    this.atcs
        .whereItemClassIs(TowerAtc.class, false)
        .forEach(q->q.setUpdatedWeatherFlag());
  }

  public void elapseSecond() {
    for (ComputerAtc atc : atcs.whereItemClassIs(
        ComputerAtc.class, true)) {
      atc.elapseSecond();
    }
  }

  public void init() {
    atcs.forEach(q -> q.init());
  }

  private Atc createAtc(eng.jAtcSim.newLib.area.Atc template) {
    Atc ret;
    switch (template.getType()) {
      case app:
        ret = new UserAtc(template);
        break;
      case twr:
        ret = new TowerAtc(template);
        break;
      case ctr:
        ret = new CenterAtc(template);
        break;
      default:
        throw new EEnumValueUnsupportedException(template.getType());
    }
    return ret;
  }
}
