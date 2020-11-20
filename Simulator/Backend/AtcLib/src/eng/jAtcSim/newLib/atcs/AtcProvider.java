package eng.jAtcSim.newLib.atcs;

import eng.eSystem.collections.EDistinctList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.utilites.CacheUsingProducer;
import eng.eSystem.utilites.NumberUtils;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.area.RunwayConfiguration;
import eng.jAtcSim.newLib.atcs.contextLocal.Context;
import eng.jAtcSim.newLib.atcs.internal.Atc;
import eng.jAtcSim.newLib.atcs.internal.UserAtc;
import eng.jAtcSim.newLib.atcs.internal.center.CenterAtc;
import eng.jAtcSim.newLib.atcs.internal.tower.TowerAtc;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.PostContracts;
import eng.jAtcSim.newLib.shared.enums.AtcType;
import eng.newXmlUtils.annotations.XmlConstructor;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class AtcProvider {

  private final AtcList<Atc> atcs;
  private final CacheUsingProducer<AtcList<AtcId>> atcIdsCache = new CacheUsingProducer<>(this::evaluateAtcIdsCache);
  private final CacheUsingProducer<AtcList<AtcId>> userAtcIdsCache = new CacheUsingProducer<>(this::evaluateUserAtcIdsCache);

  @XmlConstructor
  private AtcProvider() {
    this.atcs = null;
    PostContracts.register(this, () -> atcs != null);
  }

  public AtcProvider(Airport activeAirport) {
    this.atcs = new AtcList<>(q -> q.getAtcId(), EDistinctList.Behavior.exception);
    for (eng.jAtcSim.newLib.area.Atc atcTemplate : activeAirport.getAtcTemplates()) {
      Atc atc = createAtc(atcTemplate);
      this.atcs.add(atc);
    }

    EAssert.isTrue(activeAirport.getIcao().equals(Context.getShared().getAirportIcao()));
  }

  public void elapseSecond() {
    for (Atc atc : this.atcs) {
      atc.elapseSecond();
    }
  }

  public AtcList<AtcId> getAtcIds() {
    return atcIdsCache.get();
  }

  public int getPlanesCountAtHoldingPoint() {
    TowerAtc towerAtc = (TowerAtc) atcs.getFirst(q -> q.getAtcId().getType() == AtcType.twr);
    int ret = towerAtc.getNumberOfPlanesAtHoldingPoint();
    return ret;
  }

  public AtcId getResponsibleAtc(Callsign callsign) {
    IList<AtcId> responsibleAtcs = atcs.where(q -> q.isResponsibleFor(callsign)).select(q -> q.getAtcId());
    EAssert.isTrue(NumberUtils.isBetweenOrEqual(0, responsibleAtcs.size(), 1));
    AtcId ret = responsibleAtcs.tryGetFirst();
    return ret;
  }

  public RunwayConfiguration getRunwayConfiguration() {
    TowerAtc towerAtc = (TowerAtc) atcs.getFirst(q -> q.getAtcId().getType() == AtcType.twr);
    RunwayConfiguration ret = towerAtc.getRunwayConfigurationInUse();
    return ret;
  }

  public IReadOnlyList<AtcId> getUserAtcIds() {
    return userAtcIdsCache.get();
  }

  public IUserAtcInterface getUserAtcInterface(AtcId atcId) {
    Atc atc = this.atcs.get(atcId);
    EAssert.isTrue(atc instanceof UserAtc, () -> sf("Requested atc '%s' is not of type UserAtc.", atcId));
    UserAtc userAtc = (UserAtc) atc;
    IUserAtcInterface ret = userAtc;
    return ret;
  }

  public void init() {
    atcs.forEach(q -> q.init());
    IList<Atc> app = atcs.where(q -> q.getAtcId().getType() == AtcType.app);
    EAssert.isTrue(app.size() == 1); // application now prepared only for one app
    Context.Internal.init(
            atcs,
            app.getFirst());
  }

  public void registerNewPlane(AtcId atcId, Callsign callsign) {
    Atc atc = atcs.getFirst(q -> q.getAtcId().equals(atcId));
    atc.registerNewPlaneInGame(callsign, true);
  }

  public RunwayConfiguration tryGetSchedulerRunwayConfiguration() {
    TowerAtc towerAtc = (TowerAtc) atcs.getFirst(q -> q.getAtcId().getType() == AtcType.twr);
    RunwayConfiguration ret = towerAtc.tryGetRunwayConfigurationScheduled();
    return ret;
  }

  private AtcList<AtcId> evaluateAtcIdsCache() {
    AtcList<AtcId> ret = new AtcList<>(q -> q, EDistinctList.Behavior.exception);
    ret.addMany(this.atcs.select(q -> q.getAtcId()));
    return ret;
  }

  private AtcList<AtcId> evaluateUserAtcIdsCache() {
    AtcList<AtcId> ret = new AtcList<>(q -> q, EDistinctList.Behavior.exception);
    ret.addMany(this.atcs.where(q -> q instanceof UserAtc).select(q -> q.getAtcId()));
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
