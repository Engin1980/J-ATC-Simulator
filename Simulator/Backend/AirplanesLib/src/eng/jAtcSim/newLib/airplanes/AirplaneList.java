package eng.jAtcSim.newLib.airplanes;

import eng.eSystem.collections.EDistinctList;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.Callsign;

import java.util.Iterator;
import java.util.function.Function;

public class AirplaneList<T> extends EDistinctList<T> {
  private final Function<T, Callsign> callsignSelector;
  private T lastGot = null;


  public AirplaneList(Function<T, Callsign> callsignSelector) {
    super(q -> callsignSelector.apply(q), Behavior.exception);
    EAssert.Argument.isNotNull(callsignSelector, "callsignSelector");
    this.callsignSelector = callsignSelector;
  }

  public T get(Callsign callsign) {
    T ret;
    if (lastGot != null && callsignSelector.apply(lastGot).equals(callsign))
      ret = lastGot;
    else {
      ret = this.tryGetFirst(q -> this.callsignSelector.apply(q).equals(callsign));
      if (ret != null) lastGot = ret;
    }
    return ret;
  }
}
