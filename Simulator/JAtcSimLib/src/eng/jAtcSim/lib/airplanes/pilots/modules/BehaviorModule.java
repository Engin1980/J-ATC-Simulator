package eng.jAtcSim.lib.airplanes.pilots.modules;

import eng.jAtcSim.lib.airplanes.pilots.Pilot;
import eng.jAtcSim.lib.airplanes.pilots.behaviors.Behavior;

public class BehaviorModule {
  private final Pilot.Pilot5Module parent;
  private Behavior behavior;

  public BehaviorModule(Pilot.Pilot5Module pilot) {
    assert pilot != null;
    this.parent = pilot;
  }

  public boolean is(Class<? extends Behavior> type) {
    boolean ret = type.isAssignableFrom(behavior.getClass());
    return ret;
  }

  public <T extends Behavior> T tryGetAs(Class<T> type) {
    if (this.is(type))
      return getAs(type);
    else
      return null;
  }

  public <T extends Behavior> T getAs(Class<T> type) {
    return (T) this.behavior;
  }

  public void setBehavior(Behavior behavior) {
    assert behavior != null;
    this.behavior = behavior;
  }
}
