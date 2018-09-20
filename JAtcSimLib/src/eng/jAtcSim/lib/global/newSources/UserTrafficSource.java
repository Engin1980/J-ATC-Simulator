package eng.jAtcSim.lib.global.newSources;

import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
import eng.jAtcSim.lib.traffic.Traffic;

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
