package eng.jAtcSim.newLib.airplaneType;

import eng.eSystem.collections.EDistinctList;
import eng.eSystem.collections.IList;

public class AirplaneTypes {

  public static AirplaneTypes create(IList<AirplaneType> types) {
    AirplaneTypes ret = new AirplaneTypes();
    ret.inner.add(types);
    return ret;
  }
  private final IList<AirplaneType> inner = new EDistinctList<>(q -> q.name, EDistinctList.Behavior.exception);

  private AirplaneTypes() {
  }
}
