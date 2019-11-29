package eng.jAtcSim.lib.airplanes.modules;

import eng.jAtcSim.lib.airplanes.interfaces.IAirplaneWriteSimple;

public abstract class Module {
  protected final IAirplaneWriteSimple parent;

  protected Module(IAirplaneWriteSimple parent) {
    assert parent != null;
    this.parent = parent;
  }
}
