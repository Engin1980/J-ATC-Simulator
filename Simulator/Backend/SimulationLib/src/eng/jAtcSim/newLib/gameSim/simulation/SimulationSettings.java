package eng.jAtcSim.newLib.gameSim.simulation;

import eng.jAtcSim.newLib.gameSim.game.startupInfos.ParserFormatterStartInfo;
import eng.jAtcSim.newLib.gameSim.game.startupInfos.SimulationStartupSettingsInfo;
import eng.jAtcSim.newLib.gameSim.game.startupInfos.TrafficStartupSettingsInfo;

public class SimulationSettings {
  public final ParserFormatterStartInfo parserFormatterStartInfo;
  public final TrafficStartupSettingsInfo trafficSettings;
  public final SimulationStartupSettingsInfo simulationSettings;

  public SimulationSettings(ParserFormatterStartInfo parserFormatterStartInfo, TrafficStartupSettingsInfo trafficSettings, SimulationStartupSettingsInfo simulationSettings) {
    this.parserFormatterStartInfo = parserFormatterStartInfo;
    this.trafficSettings = trafficSettings;
    this.simulationSettings = simulationSettings;
  }
}
