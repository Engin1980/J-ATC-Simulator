package eng.jAtcSim.newLib.gameSim.simulation.modules;

import eng.jAtcSim.newLib.area.RunwayConfiguration;
import eng.jAtcSim.newLib.atcs.AtcProvider;
import eng.jAtcSim.newLib.atcs.context.AtcAcc;
import eng.jAtcSim.newLib.atcs.context.IAtcAcc;
import eng.jAtcSim.newLib.shared.ContextManager;

public class AtcModule {
  private final AtcProvider atcProvider;

  public AtcModule(AtcProvider atcProvider) {
    this.atcProvider = atcProvider;
  }

  public void adviceWeatherUpdated() {
    this.atcProvider.adviceWeatherUpdated();
  }

  public void elapseSecond() {
    atcProvider.elapseSecond();
  }

  public int getPlanesCountAtHoldingPoint() {
    int ret = atcProvider.getPlanesCountAtHoldingPoint();
    return ret;
  }

  public RunwayConfiguration getRunwayConfiguration() {
    return this.atcProvider.getRunwayConfiguration();
  }

  public void init() {
    IAtcAcc atcContext = new AtcAcc(
        this.atcProvider.getAtcIds(),
        callsign -> this.atcProvider.getResponsibleAtc(callsign));
    ContextManager.setContext(IAtcAcc.class, atcContext);
  }

  public RunwayConfiguration tryGetSchedulerRunwayConfiguration() {
    return this.atcProvider.tryGetSchedulerRunwayConfiguration();
  }
}
