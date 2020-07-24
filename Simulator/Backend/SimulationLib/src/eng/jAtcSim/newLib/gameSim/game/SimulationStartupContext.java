package eng.jAtcSim.newLib.gameSim.game;

import eng.jAtcSim.newLib.airplaneType.AirplaneTypes;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.area.Area;
import eng.jAtcSim.newLib.fleet.airliners.AirlinesFleets;
import eng.jAtcSim.newLib.fleet.generalAviation.GeneralAviationFleets;
import eng.jAtcSim.newLib.traffic.ITrafficModel;
import eng.jAtcSim.newLib.weather.WeatherProvider;

public class SimulationStartupContext {
  public final Airport activeAirport;
  public final AirlinesFleets airlinesFleets;
  public final AirplaneTypes airplaneTypes;
  public final Area area;
  public final GeneralAviationFleets gaFleets;
  public final ITrafficModel traffic;
  public final WeatherProvider weatherProvider;

  public SimulationStartupContext(Area area,
                                  String airportIcao,
                                  AirplaneTypes airplaneTypes, AirlinesFleets airlinesFleets, GeneralAviationFleets gaFleets, ITrafficModel traffic, WeatherProvider weatherProvider) {
    this.area = area;
    this.airplaneTypes = airplaneTypes;
    this.airlinesFleets = airlinesFleets;
    this.gaFleets = gaFleets;
    this.traffic = traffic;
    this.activeAirport = area.getAirports().getFirst(q -> q.getIcao().equals(airportIcao));
    this.weatherProvider = weatherProvider;
  }
}
