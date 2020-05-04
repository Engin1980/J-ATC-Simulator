package eng.jAtcSim.newLib.simulation.internal;

import eng.jAtcSim.newLib.airplaneType.AirplaneTypes;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.area.Area;
import eng.jAtcSim.newLib.fleet.airliners.AirlinesFleets;
import eng.jAtcSim.newLib.fleet.generalAviation.GeneralAviationFleets;
import eng.jAtcSim.newLib.traffic.Traffic;
import eng.jAtcSim.newLib.weather.WeatherProvider;

public class SimulationContext {
  private final Area area;
  private final AirplaneTypes airplaneTypes;
  private final AirlinesFleets airlinesFleets;
  private final GeneralAviationFleets gaFleets;
  private final Traffic traffic;
  private final Airport activeAirport;
  private final WeatherProvider weatherProvider;

  public SimulationContext(Area area, String activeAirportIcao,
                           AirplaneTypes airplaneTypes,
                           AirlinesFleets airlinesFleets, GeneralAviationFleets gaFleets,
                           Traffic traffic,
                           WeatherProvider weatherProvider) {
    this.area = area;
    this.airplaneTypes = airplaneTypes;
    this.airlinesFleets = airlinesFleets;
    this.gaFleets = gaFleets;
    this.traffic = traffic;
    this.activeAirport = area.getAirports().getFirst(q -> q.getIcao().equals(activeAirportIcao));
    this.weatherProvider = weatherProvider;
  }
}
