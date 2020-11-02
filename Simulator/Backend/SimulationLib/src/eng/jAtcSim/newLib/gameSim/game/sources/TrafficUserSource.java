package eng.jAtcSim.newLib.gameSim.game.sources;

import eng.jAtcSim.newLib.traffic.ITrafficModel;

public class TrafficUserSource extends TrafficSource {

  private final ITrafficModel traffic;

  TrafficUserSource(ITrafficModel userTraffic) {
    this.traffic = userTraffic;
  }

  @Override
  protected ITrafficModel _getContent() {
    return traffic;
  }

  @Override
  public void init() {
    super.setInitialized();
  }
}
