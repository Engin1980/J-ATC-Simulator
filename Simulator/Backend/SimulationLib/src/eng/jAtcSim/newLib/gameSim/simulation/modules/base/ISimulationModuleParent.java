package eng.jAtcSim.newLib.gameSim.simulation.modules.base;

import eng.jAtcSim.newLib.gameSim.simulation.controllers.*;
import eng.jAtcSim.newLib.gameSim.simulation.modules.AirplanesModule;
import eng.jAtcSim.newLib.gameSim.simulation.modules.IOModule;
import eng.jAtcSim.newLib.gameSim.simulation.modules.TrafficModule;
import eng.jAtcSim.newLib.gameSim.simulation.modules.WorldModule;

public interface ISimulationModuleParent {
  AirplanesModule getAirplanesModule();

  WorldModule getWorldModule();

  IOModule getIO();

  SimulationController getSimulation();

  TrafficModule getTrafficModule();
}
