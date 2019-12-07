package eng.jAtcSim.newLib.global.newSources;

import eng.jAtcSim.newLib.traffic.Traffic;

public class UserTrafficSource extends TrafficSource {

  Traffic traffic;

  public UserTrafficSource(Traffic userTraffic) {
    this.traffic = userTraffic;
  }

  @Override
  protected Traffic _getContent() {
    return traffic;
  }

  @Override
  public void init() {
    super.setInitialized();
  }
}
