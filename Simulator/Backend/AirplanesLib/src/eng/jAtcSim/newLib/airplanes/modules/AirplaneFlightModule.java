package eng.jAtcSim.newLib.airplanes.modules;


import eng.eSystem.eXml.XElement;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.contextLocal.Context;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
import exml.IPlainObjectSimPersistable;
import exml.ISimPersistable;
import exml.XContext;

public class AirplaneFlightModule implements IPlainObjectSimPersistable {
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

  public EDayTimeStamp getExitTime() {
    return this.exitTime;
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

  public void setExitTimeNow() {
    this.exitTime = Context.getShared().getNow().toStamp();
  }
}
