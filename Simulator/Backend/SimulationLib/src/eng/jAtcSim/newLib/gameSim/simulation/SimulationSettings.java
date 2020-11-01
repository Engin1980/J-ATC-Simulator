package eng.jAtcSim.newLib.gameSim.simulation;

import eng.jAtcSim.newLib.gameSim.game.startupInfos.ParserFormatterStartInfo;
import eng.jAtcSim.newLib.gameSim.game.startupInfos.SimulationStartupSettingsInfo;
import eng.jAtcSim.newLib.gameSim.game.startupInfos.TrafficStartupSettingsInfo;

public class SimulationSettings {
  public final TrafficStartupSettingsInfo trafficSettings;
  public final SimulationStartupSettingsInfo simulationSettings;

  public SimulationSettings(TrafficStartupSettingsInfo trafficSettings, SimulationStartupSettingsInfo simulationSettings) {
    this.trafficSettings = trafficSettings;
    this.simulationSettings = simulationSettings;
  }
}
