package eng.jAtcSim.newLib.atcs;

import eng.eSystem.collections.EDistinctList;
import eng.eSystem.collections.IList;
import eng.eSystem.events.EventAnonymousSimple;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.area.RunwayConfiguration;
import eng.jAtcSim.newLib.atcs.contextLocal.Context;
import eng.jAtcSim.newLib.atcs.internal.*;
import eng.jAtcSim.newLib.atcs.internal.center.CenterAtc;
import eng.jAtcSim.newLib.atcs.internal.tower.TowerAtc;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.enums.AtcType;

public class AtcProvider {
  private final AtcList<AtcId> atcIds = new AtcList<>(
      q -> q, EDistinctList.Behavior.exception);
  private final AtcList<Atc> atcs = new AtcList<>(
      q -> q.getAtcId(), EDistinctList.Behavior.exception);

  public AtcProvider(Airport activeAirport) {
    for (eng.jAtcSim.newLib.area.Atc atcTemplate : activeAirport.getAtcTemplates()) {
      Atc atc = createAtc(atcTemplate);
      atcs.add(atc);
    }

    EAssert.isTrue(activeAirport.getIcao().equals(Context.getShared().getAirportIcao()));
  }

  public void adviceWeatherUpdated() {
    this.atcs
        .whereItemClassIs(TowerAtc.class, false)
        .forEach(q -> q.setUpdatedWeatherFlag());
  }

  public void elapseSecond() {
    for (ComputerAtc atc : atcs.whereItemClassIs(
        ComputerAtc.class, true)) {
      atc.elapseSecond();
    }
  }

  public AtcList<AtcId> getAtcIds() {
    return atcIds;
  }

  public EventAnonymousSimple getOnRunwayChanged() {
    TowerAtc towerAtc = (TowerAtc) atcs.getFirst(q -> q.getAtcId().getType() == AtcType.twr);
    return towerAtc.getOnRunwayChanged();
  }

  public int getPlanesCountAtHoldingPoint() {
    TowerAtc towerAtc = (TowerAtc) atcs.getFirst(q -> q.getAtcId().getType() == AtcType.twr);
    int ret = towerAtc.getNumberOfPlanesAtHoldingPoint();
    return ret;
  }

  public AtcId getResponsibleAtc(Callsign callsign) {
    AtcId ret = null;
    for (Atc atc : atcs) {
      if (atc.isResponsibleFor(callsign)){
        ret = atc.getAtcId();
        break;
      }
    }
    return ret;
  }

  public RunwayConfiguration getRunwayConfiguration() {
    TowerAtc towerAtc = (TowerAtc) atcs.getFirst(q -> q.getAtcId().getType() == AtcType.twr);
    RunwayConfiguration ret = towerAtc.getRunwayConfigurationInUse();
    return ret;
  }

  public void init() {
    atcs.forEach(q -> q.init());
    atcs.forEach(q -> atcIds.add(q.getAtcId()));

    IList<Atc> app = atcs.where(q -> q.getAtcId().getType() == AtcType.app);
    EAssert.isTrue(app.size() == 1); // application now prepared only for one app

    Context.Internal.init(
        atcs,
        app.getFirst());
  }

  public void registerNewPlane(AtcId atcId, Callsign callsign) {
    Atc atc = atcs.getFirst(q -> q.getAtcId().equals(atcId));
    atc.registerNewPlaneUnderControl(callsign, true);
  }

  public RunwayConfiguration tryGetSchedulerRunwayConfiguration() {
    TowerAtc towerAtc = (TowerAtc) atcs.getFirst(q -> q.getAtcId().getType() == AtcType.twr);
    RunwayConfiguration ret = towerAtc.tryGetRunwayConfigurationScheduled();
    return ret;
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
