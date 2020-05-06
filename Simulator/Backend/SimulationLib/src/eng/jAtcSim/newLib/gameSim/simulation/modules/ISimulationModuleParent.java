package eng.jAtcSim.newLib.gameSim.simulation.modules;

import eng.jAtcSim.newLib.airplanes.AirplanesController;
import eng.jAtcSim.newLib.gameSim.simulation.controllers.AirproxController;
import eng.jAtcSim.newLib.gameSim.simulation.controllers.EmergencyAppearanceController;
import eng.jAtcSim.newLib.gameSim.simulation.controllers.MrvaController;
import eng.jAtcSim.newLib.gameSim.simulation.SimulationContext;
import eng.jAtcSim.newLib.traffic.TrafficProvider;

public interface ISimulationModuleParent {
  AirplanesController getAirplanesController();

  AirproxController getAirproxController();

  SimulationContext getContext();

  EmergencyAppearanceController getEmergencyAppearanceController();

  MrvaController getMrvaController();

  TrafficProvider getTrafficProvider();
}
