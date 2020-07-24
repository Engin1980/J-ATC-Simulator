package eng.jAtcSim.newLib.gameSim.simulation.modules;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplaneType.AirplaneTypes;
import eng.jAtcSim.newLib.airplaneType.context.AirplaneTypeAcc;
import eng.jAtcSim.newLib.airplaneType.context.IAirplaneTypeAcc;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.area.Area;
import eng.jAtcSim.newLib.area.context.AreaAcc;
import eng.jAtcSim.newLib.area.context.IAreaAcc;
import eng.jAtcSim.newLib.fleet.airliners.AirlinesFleets;
import eng.jAtcSim.newLib.fleet.generalAviation.GeneralAviationFleets;
import eng.jAtcSim.newLib.gameSim.game.SimulationStartupContext;
import eng.jAtcSim.newLib.gameSim.simulation.Simulation;
import eng.jAtcSim.newLib.gameSim.simulation.modules.base.SimulationModule;
import eng.jAtcSim.newLib.shared.ContextManager;
import eng.jAtcSim.newLib.traffic.ITrafficModel;
import eng.jAtcSim.newLib.weather.WeatherProvider;

public class WorldModule extends SimulationModule {
  private final Area area;
  private final AirplaneTypes airplaneTypes;
  private final AirlinesFleets airlinesFleets;
  private final GeneralAviationFleets gaFleets;
  private final ITrafficModel traffic;
  private final Airport activeAirport;
  private final WeatherProvider weatherProvider;

  public WorldModule(
      Simulation parent,
      Area area, Airport activeAirport,
                     AirplaneTypes airplaneTypes,
                     AirlinesFleets airlinesFleets, GeneralAviationFleets gaFleets,
                     ITrafficModel traffic,
                     WeatherProvider weatherProvider) {
    super(parent);
    EAssert.Argument.isTrue(area.getAirports().contains(activeAirport));
    this.area = area;
    this.airplaneTypes = airplaneTypes;
    this.airlinesFleets = airlinesFleets;
    this.gaFleets = gaFleets;
    this.traffic = traffic;
    this.activeAirport = activeAirport;
    this.weatherProvider = weatherProvider;
  }

  public WorldModule(Simulation parent, SimulationStartupContext simulationContext) {
    this(parent,
        simulationContext.area, simulationContext.activeAirport,
        simulationContext.airplaneTypes, simulationContext.airlinesFleets, simulationContext.gaFleets,
        simulationContext.traffic, simulationContext.weatherProvider);
  }

  public Area getArea() {
    return area;
  }

  public AirplaneTypes getAirplaneTypes() {
    return airplaneTypes;
  }

  public AirlinesFleets getAirlinesFleets() {
    return airlinesFleets;
  }

  public GeneralAviationFleets getGaFleets() {
    return gaFleets;
  }

  public ITrafficModel getTraffic() {
    return traffic;
  }

  public Airport getActiveAirport() {
    return activeAirport;
  }

  public WeatherProvider getWeatherProvider() {
    return weatherProvider;
  }

  public void init() {
    IAreaAcc areaAcc = new AreaAcc(
        this.area,
        this.activeAirport,
        () -> parent.getAtcModule().getRunwayConfiguration(),
        () -> parent.getAtcModule().tryGetSchedulerRunwayConfiguration()
    );
    ContextManager.setContext(IAreaAcc.class, areaAcc);

    IAirplaneTypeAcc airplaneTypeAcc = new AirplaneTypeAcc(this.airplaneTypes);
    ContextManager.setContext(IAirplaneTypeAcc.class, airplaneTypeAcc);
  }
}
