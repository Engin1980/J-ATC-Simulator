package eng.jAtcSim.newLib.simulation.internal;

import eng.jAtcSim.newLib.airplanes.AirplanesController;
import eng.jAtcSim.newLib.traffic.TrafficProvider;

public interface ISimulationModuleParent {
  AirplanesController getAirplanesController();

  AirproxController getAirproxController();

  SimulationContext getContext();

  EmergencyAppearanceController getEmergencyAppearanceController();

  MrvaController getMrvaController();

  TrafficProvider getTrafficProvider();
}
