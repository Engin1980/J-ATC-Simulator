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

public class WorldModule extends SimulationModule {
  private final Area area;
  private final AirplaneTypes airplaneTypes;
  private final AirlinesFleets airlinesFleets;
  private final GeneralAviationFleets gaFleets;
  private final Airport activeAirport;

  public WorldModule(
          Simulation parent,
          Area area, Airport activeAirport,
          AirplaneTypes airplaneTypes,
          AirlinesFleets airlinesFleets, GeneralAviationFleets gaFleets) {
    super(parent);
    EAssert.Argument.isTrue(area.getAirports().contains(activeAirport));
    this.area = area;
    this.airplaneTypes = airplaneTypes;
    this.airlinesFleets = airlinesFleets;
    this.gaFleets = gaFleets;
    this.activeAirport = activeAirport;
  }

  public WorldModule(Simulation parent, SimulationStartupContext simulationContext) {
    this(parent,
            simulationContext.area, simulationContext.activeAirport,
            simulationContext.airplaneTypes, simulationContext.airlinesFleets, simulationContext.gaFleets);
  }

  public Airport getActiveAirport() {
    return activeAirport;
  }

  public AirlinesFleets getAirlinesFleets() {
    return airlinesFleets;
  }

  public AirplaneTypes getAirplaneTypes() {
    return airplaneTypes;
  }

  public Area getArea() {
    return area;
  }

  public GeneralAviationFleets getGaFleets() {
    return gaFleets;
  }

  public void init() {
    IAreaAcc areaAcc = new AreaAcc(
            this.area,
            this.activeAirport
    );
    ContextManager.setContext(IAreaAcc.class, areaAcc);

    IAirplaneTypeAcc airplaneTypeAcc = new AirplaneTypeAcc(this.airplaneTypes);
    ContextManager.setContext(IAirplaneTypeAcc.class, airplaneTypeAcc);
  }
}
