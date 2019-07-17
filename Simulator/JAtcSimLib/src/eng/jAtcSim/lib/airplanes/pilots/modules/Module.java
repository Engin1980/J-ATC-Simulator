package eng.jAtcSim.lib.airplanes.pilots.modules;

import eng.jAtcSim.lib.airplanes.pilots.interfaces.forPilot.IPilotWriteSimple;

public abstract class Module {
  protected final IPilotWriteSimple parent;

  protected Module(IPilotWriteSimple parent) {
    assert parent != null;
    this.parent = parent;
  }
}
