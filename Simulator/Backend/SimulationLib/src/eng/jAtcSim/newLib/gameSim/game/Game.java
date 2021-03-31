package eng.jAtcSim.newLib.gameSim.game;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.gameSim.IGame;
import eng.jAtcSim.newLib.gameSim.ISimulation;
import eng.jAtcSim.newLib.gameSim.game.sources.*;
import eng.jAtcSim.newLib.gameSim.simulation.Simulation;
import exml.IXPersistable;
import exml.loading.XLoadContext;
import exml.saving.XSaveContext;

public class Game implements IGame, IXPersistable {

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
  public void xLoad(XElement elm, XLoadContext ctx) {
  }

  @Override
  public void xSave(XElement elm, XSaveContext ctx) {
    // explicitly to write content in predefined order
    ctx.fields.saveField(this, "areaSource", elm);
    ctx.fields.saveField(this, "airplaneTypesSource", elm);
    ctx.fields.saveField(this, "fleetsSource", elm);
    ctx.fields.saveField(this, "trafficSource", elm);
    ctx.fields.saveField(this, "weatherSource", elm);
    ctx.fields.saveField(this, "simulation", elm);
  }
}
