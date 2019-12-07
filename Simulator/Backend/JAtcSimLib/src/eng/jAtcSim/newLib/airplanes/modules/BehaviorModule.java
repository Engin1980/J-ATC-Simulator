package eng.jAtcSim.newLib.airplanes.modules;

import eng.jAtcSim.newLib.airplanes.behaviors.Behavior;
import eng.jAtcSim.newLib.airplanes.interfaces.IAirplaneWriteSimple;
import eng.jAtcSim.newLib.airplanes.interfaces.modules.IBehaviorModuleRO;

public class BehaviorModule extends Module implements IBehaviorModuleRO {
  private Behavior behavior;

  public BehaviorModule(IAirplaneWriteSimple parent) {
    super(parent);
  }

  public void elapseSecond() {
    behavior.fly(parent);
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
