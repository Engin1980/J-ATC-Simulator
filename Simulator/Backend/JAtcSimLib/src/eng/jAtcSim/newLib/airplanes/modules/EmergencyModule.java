package eng.jAtcSim.newLib.area.airplanes.modules;

import eng.jAtcSim.newLib.Acc;
import eng.jAtcSim.newLib.area.airplanes.interfaces.IAirplaneWriteSimple;
import eng.jAtcSim.newLib.area.airplanes.interfaces.modules.IEmergencyModuleRO;
import eng.jAtcSim.newLib.global.ETime;

public class EmergencyModule{
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
