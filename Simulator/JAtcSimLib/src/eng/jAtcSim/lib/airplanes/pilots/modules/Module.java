package eng.jAtcSim.lib.airplanes.pilots.modules;

import eng.jAtcSim.lib.airplanes.pilots.interfaces.forPilot.IPilot5Module;

public abstract class Module {
  protected final IPilot5Module parent;

  protected Module(IPilot5Module parent) {
    assert parent != null;
    this.parent = parent;
  }
}
