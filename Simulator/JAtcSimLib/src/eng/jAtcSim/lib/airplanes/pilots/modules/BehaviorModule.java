package eng.jAtcSim.lib.airplanes.pilots.modules;

import eng.jAtcSim.lib.airplanes.pilots.behaviors.Behavior;
import eng.jAtcSim.lib.airplanes.pilots.interfaces.forPilot.IBehaviorModuleRO;
import eng.jAtcSim.lib.airplanes.pilots.interfaces.forPilot.IPilotWriteSimple;

public class BehaviorModule extends Module implements IBehaviorModuleRO {
  private Behavior behavior;

  public BehaviorModule(IPilotWriteSimple parent) {
    super(parent);
  }

  @Override
  public Behavior get() {
    return this.behavior;
  }

  @Override
  public <T extends Behavior> T getAs(Class<T> type) {
    return (T) this.behavior;
  }

  @Override
  public <T extends Behavior> boolean is(Class<T> type) {
    boolean ret = type.isAssignableFrom(behavior.getClass());
    return ret;
  }

  public void setBehavior(Behavior behavior) {
    assert behavior != null;
    this.behavior = behavior;
  }
}
