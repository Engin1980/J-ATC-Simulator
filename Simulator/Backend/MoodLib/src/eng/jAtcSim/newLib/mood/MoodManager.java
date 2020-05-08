package eng.jAtcSim.newLib.mood;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.Callsign;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class MoodManager {
  private final IMap<Callsign, Mood> inner = new EMap<>();

  public Mood get(Callsign callsign) {
    EAssert.isTrue(inner.containsKey(callsign), sf("%s is not registered.", callsign));
    return inner.get(callsign);
  }

  public MoodResult getMoodResult(Callsign callsign, int delayDifference) {
    EAssert.Argument.isTrue(inner.containsKey(callsign));
    MoodResult ret = inner.get(callsign).evaluate(callsign, delayDifference);
    return ret;
  }

  public void registerCallsign(Callsign callsign) {
    EAssert.isTrue(inner.containsKey(callsign));
    inner.set(callsign, new Mood());
  }

  public void unregister(Callsign callsign) {
    EAssert.isTrue(inner.containsKey(callsign));
    inner.remove(callsign);
  }

}