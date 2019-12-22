package eng.jAtcSim.newLib.area.airplanes.interfaces.modules;

import eng.jAtcSim.newLib.area.airplanes.behaviors.Behavior;

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
