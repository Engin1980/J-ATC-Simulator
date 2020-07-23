package eng.jAtcSim.newLib.gameSim.simulation.modules;

import eng.jAtcSim.newLib.airplanes.AirplanesController;
import eng.jAtcSim.newLib.gameSim.simulation.controllers.*;
import eng.jAtcSim.newLib.gameSim.simulation.WorldModule;
import eng.jAtcSim.newLib.traffic.TrafficProvider;

public interface ISimulationModuleParent {
  AirplanesController getAirplanesController();

  AirproxController getAirproxController();

  WorldModule getContext();

  EmergencyAppearanceController getEmergencyAppearanceController();

  IOController getIO();

  MrvaController getMrvaController();

  SimulationController getSimulation();

  TrafficProvider getTrafficProvider();
}
