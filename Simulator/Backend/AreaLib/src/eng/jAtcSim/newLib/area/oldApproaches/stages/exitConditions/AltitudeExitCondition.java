package eng.jAtcSim.newLib.area.oldApproaches.stages.exitConditions;

import eng.eSystem.collections.EMap;
import eng.jAtcSim.newLib.area.oldApproaches.stages.IExitCondition;

public class AltitudeExitCondition implements IExitCondition {

  public enum eDirection{
    above,
    below
  }

  private final EMap<Character, Integer> inner = new EMap<>();
  private final eDirection direction;

  public AltitudeExitCondition(eDirection direction, int altitude) {
    this(direction,altitude, altitude, altitude, altitude);
  }

  public AltitudeExitCondition(eDirection direction, int categoryAAltitude, int categoryBAltitude, int categoryCAltitude, int categoryDAltitude) {
    assert categoryAAltitude > 0;
    assert categoryBAltitude > 0;
    assert categoryCAltitude > 0;
    assert categoryDAltitude > 0;
    this.direction = direction;
    inner.set('A', categoryAAltitude);
    inner.set('B', categoryBAltitude);
    inner.set('C', categoryCAltitude);
    inner.set('D', categoryDAltitude);
  }
}
