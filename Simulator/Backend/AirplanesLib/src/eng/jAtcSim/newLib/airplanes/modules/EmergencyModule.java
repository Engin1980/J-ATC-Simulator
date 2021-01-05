package eng.jAtcSim.newLib.airplanes.modules;


import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
import exml.IXPersistable;

public class EmergencyModule implements IXPersistable {
  private EDayTimeStamp emergencyWanishTime = null;

  //TODO narvat emergency generating here s pomocí static items

  public boolean isEmergency() {
    return this.emergencyWanishTime != null;
  }

  public void setEmergencyWanishTime(EDayTimeStamp emergencyWanishTime) {
    this.emergencyWanishTime = emergencyWanishTime;
  }
}
