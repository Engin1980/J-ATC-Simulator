package eng.jAtcSim.newLib.airplanes;

import eng.eSystem.collections.EDistinctList;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.functionalInterfaces.Selector;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.Squawk;

import java.util.Iterator;
import java.util.function.Function;

public class BaseAirplaneList<T> extends EDistinctList<T> {
  private final Selector<T, Callsign> callsignSelector;
  private final Selector<T, Squawk> squawkSelector;
  private T lastGot = null;

  public BaseAirplaneList(Selector<T, Callsign> callsignSelector, Selector<T, Squawk> squawkSelector) {
    super(q -> callsignSelector.invoke(q), Behavior.exception);
    EAssert.Argument.isNotNull(callsignSelector, "callsignSelector");
    EAssert.Argument.isNotNull(squawkSelector, "squawkSelector");
    this.callsignSelector = callsignSelector;
    this.squawkSelector = squawkSelector;
  }

  public T get(Callsign callsign) {
    T ret;
    if (lastGot != null && callsignSelector.invoke(lastGot).equals(callsign))
      ret = lastGot;
    else {
      ret = this.tryGetFirst(q -> this.callsignSelector.invoke(q).equals(callsign));
      if (ret != null) lastGot = ret;
    }
    EAssert.isNotNull(ret, "AirplaneList did not found matching plane for callsign: " + callsign);
    return ret;
  }

  public T get(Squawk squawk){
    return this.getFirst(q->squawkSelector.invoke(q).equals(squawk));
  }

  public T tryGet(Squawk squawk) {
    return this.tryGetFirst(q->squawkSelector.invoke(q).equals(squawk));
  }

  public T tryGet(Callsign callsign) {
    return this.tryGetFirst(q->callsignSelector.invoke(q).equals(callsign));
  }
}
