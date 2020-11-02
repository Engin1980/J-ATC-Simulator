package eng.jAtcSim.newLib.gameSim.simulation;

import eng.jAtcSim.newLib.gameSim.game.startupInfos.TrafficSettings;

public class SimulationSettings {
  public final TrafficSettings trafficSettings;
  public final eng.jAtcSim.newLib.gameSim.game.startupInfos.SimulationSettings simulationSettings;

  public SimulationSettings(TrafficSettings trafficSettings, eng.jAtcSim.newLib.gameSim.game.startupInfos.SimulationSettings simulationSettings) {
    this.trafficSettings = trafficSettings;
    this.simulationSettings = simulationSettings;
  }
}
