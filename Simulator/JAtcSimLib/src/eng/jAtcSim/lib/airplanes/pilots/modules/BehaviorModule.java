package eng.jAtcSim.lib.airplanes.pilots.modules;

import eng.jAtcSim.lib.airplanes.pilots.Pilot;
import eng.jAtcSim.lib.airplanes.pilots.behaviors.Behavior;
import eng.jAtcSim.lib.airplanes.pilots.interfaces.forPilot.IPilot5Module;

public class BehaviorModule extends Module{
  private Behavior behavior;

  public BehaviorModule(IPilot5Module parent) {
    super(parent);
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
