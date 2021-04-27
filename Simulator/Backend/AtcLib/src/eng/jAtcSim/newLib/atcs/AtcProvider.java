package eng.jAtcSim.newLib.atcs;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.UnexpectedValueException;
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
import eng.jAtcSim.newLib.shared.AtcIdList;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.PostContracts;
import eng.jAtcSim.newLib.shared.enums.AtcType;

import exml.IXPersistable;
import exml.loading.XLoadContext; import exml.saving.XSaveContext;
import exml.annotations.XConstructor;
import exml.annotations.XIgnored;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class AtcProvider implements IXPersistable {

  private final AtcList atcs;
  @XIgnored
  private final CacheUsingProducer<AtcIdList> atcIdsCache = new CacheUsingProducer<>(this::evaluateAtcIdsCache);
  @XIgnored
  private final CacheUsingProducer<AtcIdList> userAtcIdsCache = new CacheUsingProducer<>(this::evaluateUserAtcIdsCache);


  @XConstructor
  private AtcProvider() {
    this.atcs = null;
    PostContracts.register(this, () -> atcs != null);
  }

  public AtcProvider(Airport activeAirport) {
    this.atcs = new AtcList();
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

  public AtcIdList getAtcIds() {
    return atcIdsCache.get();
  }

  public int getPlanesCountAtHoldingPoint() {
    TowerAtc towerAtc = (TowerAtc) atcs.getFirst(q -> q.getAtcId().getType() == AtcType.twr);
    int ret = towerAtc.getNumberOfPlanesAtHoldingPoint();
    return ret;
  }

  public AtcId tryGetResponsibleAtc(Callsign callsign) {
    IList<AtcId> responsibleAtcs = atcs.where(q -> q.isResponsibleFor(callsign)).select(q -> q.getAtcId());
    EAssert.isTrue(NumberUtils.isBetweenOrEqual(0, responsibleAtcs.size(), 1));
    AtcId ret = responsibleAtcs.tryGetFirst().orElse(null);
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

  @Override
  public void xLoad(XElement elm, XLoadContext ctx) {
    ctx.fields.loadFieldItems(this, "atcs", new AtcList(), Atc.class, elm);
  }

  public void registerNewPlane(AtcId atcId, Callsign callsign) {
    Atc atc = atcs.getFirst(q -> q.getAtcId().equals(atcId));
    atc.registerNewPlaneInGame(callsign, true);
  }

  @Override
  public void xSave(XElement elm, XSaveContext ctx) {
    ctx.fields.saveFieldItems(this, "atcs", Atc.class, elm);
  }

  public RunwayConfiguration tryGetSchedulerRunwayConfiguration() {
    TowerAtc towerAtc = (TowerAtc) atcs.getFirst(q -> q.getAtcId().getType() == AtcType.twr);
    RunwayConfiguration ret = towerAtc.tryGetRunwayConfigurationScheduled();
    return ret;
  }

  public void unregisterPlane(Callsign callsign, boolean isForced) {
    this.atcs.forEach(q -> q.unregisterPlaneDeletedFromGame(callsign, isForced));
  }

  private AtcIdList evaluateAtcIdsCache() {
    AtcIdList ret = new AtcIdList();
    ret.addMany(this.atcs.select(q -> q.getAtcId()));
    return ret;
  }

  private AtcIdList evaluateUserAtcIdsCache() {
    AtcIdList ret = new AtcIdList();
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
        throw new UnexpectedValueException(template.getType());
    }
    return ret;
  }
}
