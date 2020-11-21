package eng.jAtcSim.newLib.shared.context;

import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.logging.SimulationLog;
import eng.jAtcSim.newLib.shared.time.EDayTimeRun;

public class NonSimSharedAcc implements ISharedAcc {
  private final String airportIcao;
  private final IReadOnlyList<AtcId> atcIds;

  public NonSimSharedAcc(String airportIcao, IReadOnlyList<AtcId> atcIds) {
    EAssert.Argument.isNotNull(airportIcao, "airportIcao");
    EAssert.Argument.isNotNull(atcIds, "atcIds");

    this.airportIcao = airportIcao;
    this.atcIds = atcIds;
  }

  @Override
  public String getAirportIcao() {
    return airportIcao;
  }

  @Override
  public IReadOnlyList<AtcId> getAtcs() {
    return atcIds;
  }

  @Override
  public EDayTimeRun getNow() {
    throw new UnsupportedOperationException();
  }

  @Override
  public SimulationLog getSimLog() {
    throw new UnsupportedOperationException();
  }
}
