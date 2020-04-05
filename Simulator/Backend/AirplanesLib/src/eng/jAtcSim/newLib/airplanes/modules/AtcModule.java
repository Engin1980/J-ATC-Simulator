package eng.jAtcSim.newLib.airplanes.modules;

import eng.eSystem.collections.*;
import eng.eSystem.validation.EAssert;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class AtcModule {
  //TODO delete if not used
  //  private int altitudeOrderedByAtc;
  private String atcName;
  private int secondsWithoutRadarContact = 0;

  public String getTunedAtc() {
    return this.atcName;
  }

  public boolean hasRadarContact() {
    return secondsWithoutRadarContact == 0;
  }
//
//  public void requestRadarContactIfRequired() {
//    if (this.secondsWithoutRadarContact > 0) {
//      this.secondsWithoutRadarContact++;
//      if (this.secondsWithoutRadarContact % Global.REPEATED_RADAR_CONTACT_REQUEST_SECONDS == 0) {
//        parent.sendMessage(
//            new GoodDayNotification(
//                parent.getFlightModule().getCallsign(),
//                parent.getSha().getAltitude(),
//                parent.getSha().getTargetAltitude(),
//                parent.getEmergencyModule().isEmergency(),
//                true));
//      }
//    }
//  }

  public int getSecondsWithoutRadarContact() {
    return this.secondsWithoutRadarContact;
  }

  public void setHasRadarContact() {
    this.secondsWithoutRadarContact = 0;
  }

  public void changeAtc(String atcName) {
    EAssert.Argument.isNonemptyString(atcName);
    this.atcName = atcName;
    this.secondsWithoutRadarContact = 1;
  }
}
