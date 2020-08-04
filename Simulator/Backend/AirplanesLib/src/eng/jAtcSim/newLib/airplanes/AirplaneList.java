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

public class AirplaneList<T> extends EDistinctList<T> {
  private final Selector<T, Callsign> callsignSelector;
  private final Selector<T, Squawk> squawkSelector;
  private T lastGot = null;


  public AirplaneList(Selector<T, Callsign> callsignSelector, Selector<T, Squawk> squawkSelector) {
    super(q -> callsignSelector.getValue(q), Behavior.exception);
    EAssert.Argument.isNotNull(callsignSelector, "callsignSelector");
    EAssert.Argument.isNotNull(squawkSelector, "squawkSelector");
    this.callsignSelector = callsignSelector;
    this.squawkSelector = squawkSelector;
  }

  public T get(Callsign callsign) {
    T ret;
    if (lastGot != null && callsignSelector.getValue(lastGot).equals(callsign))
      ret = lastGot;
    else {
      ret = this.tryGetFirst(q -> this.callsignSelector.getValue(q).equals(callsign));
      if (ret != null) lastGot = ret;
    }
    return ret;
  }

  public T get(Squawk squawk){
    return this.getFirst(q->squawkSelector.getValue(q).equals(squawk));
  }

  public T tryGet(Squawk squawk) {
    return this.tryGetFirst(q->squawkSelector.getValue(q).equals(squawk));
  }

  public T tryGet(Callsign callsign) {
    return this.tryGetFirst(q->callsignSelector.getValue(q).equals(callsign));
  }
}
