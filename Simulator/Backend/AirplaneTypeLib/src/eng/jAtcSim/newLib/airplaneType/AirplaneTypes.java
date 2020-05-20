package eng.jAtcSim.newLib.airplaneType;

import eng.eSystem.collections.EDistinctList;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;

public class AirplaneTypes {

  public static AirplaneTypes create(IList<AirplaneType> types) {
    AirplaneTypes ret = new AirplaneTypes();
    ret.inner.add(types);
    ret.typeNamesOnly.add(types.select(q -> q.name));
    return ret;
  }

  private final IList<AirplaneType> inner = new EDistinctList<>(q -> q.name, EDistinctList.Behavior.exception);
  private final IList<String> typeNamesOnly = new EList<>();

  public AirplaneType tryGetByName(String airplaneTypeName) {
    return inner.tryGetFirst(q->q.name.equals(airplaneTypeName));
  }

  public IReadOnlyList<String> getTypeNames() {
    return typeNamesOnly;
  }

  private AirplaneTypes() {
  }


}
