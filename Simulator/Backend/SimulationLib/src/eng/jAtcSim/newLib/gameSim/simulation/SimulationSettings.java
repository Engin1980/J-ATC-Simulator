package eng.jAtcSim.newLib.gameSim.simulation;

import eng.jAtcSim.newLib.gameSim.game.startupInfos.SimulationStartupSettingsInfo;
import eng.jAtcSim.newLib.gameSim.game.startupInfos.TrafficStartupSettingsInfo;
import eng.jAtcSim.newLib.textProcessing.base.Formatter;
import eng.jAtcSim.newLib.textProcessing.base.Parser;

public class SimulationSettings {
  public final Parser parser;
  public final Formatter formatter;
  public final TrafficStartupSettingsInfo trafficSettings;
  public final SimulationStartupSettingsInfo simulationSettings;

  public SimulationSettings(Parser parser, Formatter formatter, TrafficStartupSettingsInfo trafficSettings, SimulationStartupSettingsInfo simulationSettings) {
    this.parser = parser;
    this.formatter = formatter;
    this.trafficSettings = trafficSettings;
    this.simulationSettings = simulationSettings;
  }
}
