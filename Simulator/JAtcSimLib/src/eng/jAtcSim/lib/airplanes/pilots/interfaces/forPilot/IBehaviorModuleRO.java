package eng.jAtcSim.lib.airplanes.pilots.interfaces.forPilot;

import eng.jAtcSim.lib.airplanes.pilots.behaviors.Behavior;

public interface IBehaviorModuleRO {
  Behavior get();

  <T extends Behavior> T getAs(Class<T> type);

  <T extends Behavior> boolean is(Class<T> type);

  default <T extends Behavior> T tryGetAs(Class<T> type) {
    if (this.is(type)) {
      return getAs(type);
    } else
      return null;
  }
}
