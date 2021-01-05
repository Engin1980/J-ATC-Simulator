package eng.jAtcSim.newLib.gameSim.game;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.gameSim.IGame;
import eng.jAtcSim.newLib.gameSim.ISimulation;
import eng.jAtcSim.newLib.gameSim.game.sources.*;
import eng.jAtcSim.newLib.gameSim.simulation.Simulation;
import exml.ISimPersistable;
import exml.XContext;

public class Game implements IGame, ISimPersistable {

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

  @Override
  public void save(XElement elm, XContext ctx) {
    ctx.saver.saveField(this, "areaSource", elm);
    ctx.saver.saveField(this, "airplaneTypesSource", elm);
    ctx.saver.saveField(this, "fleetsSource", elm);
    ctx.saver.saveField(this, "trafficSource", elm);
    ctx.saver.saveField(this, "weatherSource", elm);
    ctx.saver.saveRemainingFields(this, elm);
  }

  @Override
  public void load(XElement elm, XContext ctx) {

  }
}
