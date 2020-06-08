package eng.jAtcSim.newLib.gameSim;

import eng.eSystem.collections.IMap;

public interface IGame {
  ISimulation getSimulation();

  void save(String toString, IMap<String, Object> tmp);
}
