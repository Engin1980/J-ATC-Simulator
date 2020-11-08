package eng.jAtcSim.newLib.airplanes.modules;


import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
import eng.jAtcSimLib.xmlUtils.XmlSaveUtils;

public class EmergencyModule {
  private EDayTimeStamp emergencyWanishTime = null;

  //TODO narvat emergency generating here s pomocí static items

  public boolean isEmergency() {
    return this.emergencyWanishTime != null;
  }

  public void setEmergencyWanishTime(EDayTimeStamp emergencyWanishTime) {
    this.emergencyWanishTime = emergencyWanishTime;
  }
}
