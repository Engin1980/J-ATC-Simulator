package eng.jAtcSim.newLib.gameSim.simulation.modules;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.traffic.TrafficProvider;

public class TrafficModule {
  private final TrafficProvider trafficProvider;
  public TrafficModule(TrafficProvider trafficProvider) {
    EAssert.Argument.isNotNull(trafficProvider, "trafficProvider");
    this.trafficProvider = trafficProvider;
  }
}
