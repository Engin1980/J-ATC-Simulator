package eng.jAtcSim.newLib.airplanes.modules;


import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;

public class EmergencyModule{
  private EDayTimeStamp emergencyWanishTime = null;

  public boolean isEmergency() {
    return this.emergencyWanishTime != null;
  }

  public void setEmergencyWanishTime(EDayTimeStamp emergencyWanishTime) {
    this.emergencyWanishTime = emergencyWanishTime;
  }
}
