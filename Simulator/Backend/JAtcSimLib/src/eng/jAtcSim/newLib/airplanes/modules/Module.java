package eng.jAtcSim.newLib.area.airplanes.modules;

import eng.jAtcSim.newLib.area.airplanes.interfaces.IAirplaneWriteSimple;

public abstract class Module {
  protected final IAirplaneWriteSimple parent;

  protected Module(IAirplaneWriteSimple parent) {
    assert parent != null;
    this.parent = parent;
  }
}
