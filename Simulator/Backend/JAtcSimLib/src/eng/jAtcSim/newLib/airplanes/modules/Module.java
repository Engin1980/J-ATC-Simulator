package eng.jAtcSim.newLib.airplanes.modules;

import eng.jAtcSim.newLib.airplanes.interfaces.IAirplaneWriteSimple;

public abstract class Module {
  protected final IAirplaneWriteSimple parent;

  protected Module(IAirplaneWriteSimple parent) {
    assert parent != null;
    this.parent = parent;
  }
}
