package eng.jAtcSim.newLib.gameSim.game.sources;

import eng.jAtcSim.newLib.traffic.ITrafficModel;

public abstract class TrafficSource extends Source<ITrafficModel> {

  public abstract void init();

}
