package eng.jAtcSim.lib.airplanes.modules;

import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.interfaces.IAirplaneWriteSimple;
import eng.jAtcSim.lib.airplanes.interfaces.modules.IEmergencyModuleRO;
import eng.jAtcSim.lib.global.ETime;

public class EmergencyModule extends Module implements IEmergencyModuleRO {
  private ETime emergencyWanishTime = null;

  public EmergencyModule(IAirplaneWriteSimple parent) {
    super(parent);
  }

  public boolean hasElapsedEmergencyTime() {
    assert this.emergencyWanishTime != null;
    boolean ret = this.emergencyWanishTime.isBefore(Acc.now());
    return ret;
  }

  public boolean isEmergency() {
    return this.emergencyWanishTime != null;
  }

  public void setEmergencyWanishTime(ETime emergencyWanishTime) {
    this.emergencyWanishTime = emergencyWanishTime;
  }
}
