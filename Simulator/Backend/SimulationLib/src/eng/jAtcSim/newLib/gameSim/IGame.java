package eng.jAtcSim.newLib.gameSim;

import eng.eSystem.collections.IMap;

public interface IGame {
  ISimulation getSimulation();

  void save(String fileName, IMap<String, String> customData);
}
