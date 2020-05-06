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

  public void registerCallsign(Callsign callsign) {
    EAssert.isTrue(inner.containsKey(callsign));
    inner.set(callsign, new Mood());
  }

  public void unregister(Callsign callsign) {
    EAssert.isTrue(inner.containsKey(callsign));
    inner.remove(callsign);
  }

}
