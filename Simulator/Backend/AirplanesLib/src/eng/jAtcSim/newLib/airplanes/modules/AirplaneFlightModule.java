package eng.jAtcSim.newLib.airplanes.modules;


import eng.eSystem.eXml.XElement;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
import eng.jAtcSim.newLib.shared.xml.SmartXmlLoaderUtils;
import eng.jAtcSimLib.xmlUtils.ObjectUtils;
import eng.jAtcSimLib.xmlUtils.XmlSaveUtils;

public class AirplaneFlightModule {
  private final Callsign callsign;
  private final int entryDelay;
  private final EDayTimeStamp expectedExitTime;
  private EDayTimeStamp exitTime = null;
  private boolean departure;

  public AirplaneFlightModule(Callsign callsign, int entryDelay, EDayTimeStamp expectedExitTime, boolean departure) {
    this.callsign = callsign;
    this.entryDelay = entryDelay;
    this.expectedExitTime = expectedExitTime;
    this.departure = departure;
  }

  public void divert() {
    this.departure = true;
  }

  public Callsign getCallsign() {
    return callsign;
  }

  public int getEntryDelay() {
    return entryDelay;
  }

  public int getExitDelay() {
    EAssert.isTrue(exitTime != null, "Exit time was not registered, so exit delay cannot be calculated for.");
    int diff = exitTime.getValue() - expectedExitTime.getValue();
    diff = diff / 60; // to minutes
    return diff;
  }

  public EDayTimeStamp getExpectedExitTime() {
    return expectedExitTime;
  }

  public boolean isArrival() {
    return !isDeparture();
  }

  public boolean isDeparture() {
    return departure;
  }

  public void raiseEmergency() {
    this.departure = false;
  }

  public void save(XElement target) {
    XmlSaveUtils.Field.storeFields(target, this,
            ObjectUtils.getFieldNames(AirplaneFlightModule.class));
  }
}
