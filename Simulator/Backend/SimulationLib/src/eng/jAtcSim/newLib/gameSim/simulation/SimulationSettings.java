package eng.jAtcSim.newLib.gameSim.simulation;

import eng.jAtcSim.newLib.gameSim.game.startupInfos.SimulationStartupSettingsInfo;
import eng.jAtcSim.newLib.gameSim.game.startupInfos.TrafficStartupSettingsInfo;
import eng.jAtcSim.newLib.textProcessing.base.Formatter;
import eng.jAtcSim.newLib.textProcessing.base.IParser;

public class SimulationSettings {
  public final IParser parser;
  public final Formatter formatter;
  public final TrafficStartupSettingsInfo trafficSettings;
  public final SimulationStartupSettingsInfo simulationSettings;

  public SimulationSettings(IParser parser, Formatter formatter, TrafficStartupSettingsInfo trafficSettings, SimulationStartupSettingsInfo simulationSettings) {
    this.parser = parser;
    this.formatter = formatter;
    this.trafficSettings = trafficSettings;
    this.simulationSettings = simulationSettings;
  }
}
