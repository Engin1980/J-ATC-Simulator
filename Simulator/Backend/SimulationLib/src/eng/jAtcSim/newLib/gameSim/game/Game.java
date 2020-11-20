package eng.jAtcSim.newLib.gameSim.game;

import eng.jAtcSim.newLib.area.context.AreaAcc;
import eng.jAtcSim.newLib.area.context.IAreaAcc;
import eng.jAtcSim.newLib.gameSim.IGame;
import eng.jAtcSim.newLib.gameSim.ISimulation;
import eng.jAtcSim.newLib.gameSim.game.sources.*;
import eng.jAtcSim.newLib.gameSim.simulation.Simulation;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.ContextManager;
import eng.jAtcSim.newLib.shared.context.ISharedAcc;
import eng.jAtcSim.newLib.shared.context.SharedAcc;
import eng.jAtcSim.newLib.shared.logging.SimulationLog;
import eng.jAtcSim.newLib.shared.time.EDayTimeRun;
import eng.newXmlUtils.XmlContext;
import eng.newXmlUtils.implementations.ObjectDeserializer;
import eng.newXmlUtils.implementations.ObjectSerializer;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class Game implements IGame {

  private AreaSource areaSource;
  private AirplaneTypesSource airplaneTypesSource;
  private FleetsSource fleetsSource;
  private TrafficSource trafficSource;
  private WeatherSource weatherSource;
  private Simulation simulation;

  private Game() {
  }

  public Game(AreaSource areaSource, AirplaneTypesSource airplaneTypesSource, FleetsSource fleetsSource, TrafficSource trafficSource, WeatherSource weatherSource, Simulation simulation) {
    this.areaSource = areaSource;
    this.airplaneTypesSource = airplaneTypesSource;
    this.fleetsSource = fleetsSource;
    this.trafficSource = trafficSource;
    this.weatherSource = weatherSource;
    this.simulation = simulation;
  }

  @Override
  public ISimulation getSimulation() {
    return this.simulation.isim;
  }

}
