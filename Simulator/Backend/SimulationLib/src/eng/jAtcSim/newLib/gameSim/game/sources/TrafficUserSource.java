package eng.jAtcSim.newLib.gameSim.game.sources;

import eng.jAtcSim.newLib.traffic.ITrafficModel;

public class TrafficUserSource extends TrafficSource {

  TrafficUserSource(ITrafficModel userTraffic) {
    this.setContent(userTraffic);
  }

  @Override
  public void init() {
    // done in .ctor
  }
}
