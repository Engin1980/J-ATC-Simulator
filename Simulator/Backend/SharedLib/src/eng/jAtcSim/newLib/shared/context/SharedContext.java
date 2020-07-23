package eng.jAtcSim.newLib.shared.context;

import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.logging.SimulationLog;
import eng.jAtcSim.newLib.shared.time.EDayTimeRun;

public class SharedContext implements ISharedContext {
  private final String airportIcao;
  private final IReadOnlyList<AtcId> atcs;
  private final EDayTimeRun now;
  private final SimulationLog simLog;

  public SharedContext(String airportIcao, IReadOnlyList<AtcId> atcs, EDayTimeRun now, SimulationLog simLog) {
    EAssert.Argument.isNotNull(airportIcao, "airportIcao");
    EAssert.Argument.isNotNull(atcs, "atcs");
    EAssert.Argument.isNotNull(now, "now");
    EAssert.Argument.isNotNull(simLog, "simLog");

    this.airportIcao = airportIcao;
    this.atcs = atcs;
    this.now = now;
    this.simLog = simLog;
  }

  @Override
  public String getAirportIcao() {
    return airportIcao;
  }

  @Override
  public IReadOnlyList<AtcId> getAtcs() {
    return atcs;
  }

  @Override
  public EDayTimeRun getNow() {
    return now;
  }

  @Override
  public SimulationLog getSimLog() {
    return simLog;
  }
}
