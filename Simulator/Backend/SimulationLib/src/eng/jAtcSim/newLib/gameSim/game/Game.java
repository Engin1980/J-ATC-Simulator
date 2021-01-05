package eng.jAtcSim.newLib.gameSim.game;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.gameSim.IGame;
import eng.jAtcSim.newLib.gameSim.ISimulation;
import eng.jAtcSim.newLib.gameSim.game.sources.*;
import eng.jAtcSim.newLib.gameSim.simulation.Simulation;
import exml.IXPersistable;
import exml.XContext;
import exml.annotations.XIgnored;

public class Game implements IGame, IXPersistable {

  @XIgnored private AreaSource areaSource;
  @XIgnored private AirplaneTypesSource airplaneTypesSource;
  @XIgnored private FleetsSource fleetsSource;
  @XIgnored private TrafficSource trafficSource;
  @XIgnored private WeatherSource weatherSource;
  @XIgnored private Simulation simulation;

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
    ctx.saver.saveField(this, "simulation", elm);
  }

  @Override
  public void load(XElement elm, XContext ctx) {

  }
}
