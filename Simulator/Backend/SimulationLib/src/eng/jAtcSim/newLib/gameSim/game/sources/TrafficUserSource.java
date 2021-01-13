package eng.jAtcSim.newLib.gameSim.game.sources;

import eng.jAtcSim.newLib.traffic.ITrafficModel;
import exml.annotations.XConstructor;

public class TrafficUserSource extends TrafficSource {

  @XConstructor
  private TrafficUserSource() {
  }

  TrafficUserSource(ITrafficModel userTraffic) {
    this.setContent(userTraffic);
  }

  @Override
  public void init() {
    // done in .ctor
  }
}
