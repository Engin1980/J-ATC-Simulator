package eng.jAtcSim.newLib.gameSim.simulation.modules;

import eng.eSystem.collections.IList;
import eng.eSystem.events.EventAnonymousSimple;
import eng.eSystem.events.IEventListenerAnonymousSimple;
import eng.eSystem.events.IEventListenerSimple;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.area.RunwayConfiguration;
import eng.jAtcSim.newLib.atcs.AtcList;
import eng.jAtcSim.newLib.atcs.AtcProvider;
import eng.jAtcSim.newLib.atcs.context.AtcAcc;
import eng.jAtcSim.newLib.atcs.context.IAtcAcc;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.ContextManager;

public class AtcModule {
  private final AtcProvider atcProvider;
  private final AtcId userAtcId;

  public AtcModule(AtcId userAtcId, AtcProvider atcProvider) {
    EAssert.Argument.isNotNull(userAtcId, "userAtcId");
    EAssert.Argument.isNotNull(atcProvider, "atcProvider");

    this.userAtcId = userAtcId;
    this.atcProvider = atcProvider;
  }

  public EventAnonymousSimple getOnRunwayChanged() {
    return atcProvider.getOnRunwayChanged();
  }

  public AtcId getUserAtcId() {
    return userAtcId;
  }

  public void adviceWeatherUpdated() {
    this.atcProvider.adviceWeatherUpdated();
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

  public RunwayConfiguration getRunwayConfiguration() {
    return this.atcProvider.getRunwayConfiguration();
  }

  public AtcId getUserAtc() {
    return this.userAtcId;
  }

  public void init() {
    IAtcAcc atcContext = new AtcAcc(
        this.atcProvider.getAtcIds(),
        callsign -> this.atcProvider.getResponsibleAtc(callsign));
    ContextManager.setContext(IAtcAcc.class, atcContext);
    this.atcProvider.init();
  }

  public RunwayConfiguration tryGetSchedulerRunwayConfiguration() {
    return this.atcProvider.tryGetSchedulerRunwayConfiguration();
  }
}
