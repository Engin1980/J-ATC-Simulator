package eng.jAtcSim.newLib.gameSim.simulation.modules;

import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.utilites.CacheUsingProducer;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.IAirplane;
import eng.jAtcSim.newLib.area.RunwayConfiguration;
import eng.jAtcSim.newLib.atcs.AtcList;
import eng.jAtcSim.newLib.atcs.AtcProvider;
import eng.jAtcSim.newLib.atcs.IUserAtcInterface;
import eng.jAtcSim.newLib.atcs.context.AtcAcc;
import eng.jAtcSim.newLib.atcs.context.IAtcAcc;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.ContextManager;
import eng.newXmlUtils.annotations.XmlConstructor;
import exml.IXPersistable;
import exml.XContext;
import exml.annotations.XIgnored;

public class AtcModule implements IXPersistable {
  private final AtcProvider atcProvider;
  @XIgnored private final CacheUsingProducer<IReadOnlyList<AtcId>> userAtcsCache = new CacheUsingProducer<>(this::evaluateUserAtcs);

  public AtcModule(AtcProvider atcProvider) {
    EAssert.Argument.isNotNull(atcProvider, "atcProvider");
    this.atcProvider = atcProvider;
  }

  @XmlConstructor
  private AtcModule() {
    this.atcProvider = null;
  }

  public void elapseSecond() {
    atcProvider.elapseSecond();
  }

  public AtcList<AtcId> getAtcs() {
    return atcProvider.getAtcIds();
  }

  public int getPlanesCountAtHoldingPoint() {
    int ret = atcProvider.getPlanesCountAtHoldingPoint();
    return ret;
  }

  public AtcId getResponsibleAtc(IAirplane airplane) {
    return atcProvider.getResponsibleAtc(airplane.getCallsign());
  }

  public RunwayConfiguration getRunwayConfiguration() {
    return this.atcProvider.getRunwayConfiguration();
  }

  public IReadOnlyList<AtcId> getUserAtcIds() {
    return userAtcsCache.get();
  }

  public IUserAtcInterface getUserAtcInterface(AtcId atcId) {
    return atcProvider.getUserAtcInterface(atcId);
  }

  public void init() {
    IAtcAcc atcContext = new AtcAcc(
            this.atcProvider.getAtcIds(),
            callsign -> this.atcProvider.getResponsibleAtc(callsign));
    ContextManager.setContext(IAtcAcc.class, atcContext);
    this.atcProvider.init();
  }

  public void registerNewPlane(IAirplane tmp) {
    this.atcProvider.registerNewPlane(tmp.getAtc().getTunedAtc(), tmp.getCallsign());
  }

  public void unregisterPlane(Callsign callsign, boolean isForced){
    this.atcProvider.unregisterPlane(callsign, isForced);
  }

  public RunwayConfiguration tryGetSchedulerRunwayConfiguration() {
    return this.atcProvider.tryGetSchedulerRunwayConfiguration();
  }

  private IReadOnlyList<AtcId> evaluateUserAtcs() {
    return atcProvider.getUserAtcIds();
  }
}
