//package eng.jAtcSim.newLib.area.airplanes.modules;
//
//import eng.jAtcSim.newLib.area.airplanes.interfaces.IAirplaneWriteSimple;
//import eng.jAtcSim.newLib.area.airplanes.interfaces.modules.IAtcModuleRO;
//import eng.jAtcSim.newLib.area.atcs.Atc;
//import eng.jAtcSim.newLib.global.Global;
//import eng.jAtcSim.newLib.area.speaking.fromAirplane.notifications.GoodDayNotification;
//
//public class AtcModule extends Module implements IAtcModuleRO {
//  //TODO delete if not used
//  //  private int altitudeOrderedByAtc;
//  private Atc atc;
//  private int secondsWithoutRadarContact = 0;
//
//  public AtcModule(IAirplaneWriteSimple parent) {
//    super(parent);
//  }
//
//  public void elapseSecond() {
//    requestRadarContactIfRequired();
//  }
//
//  public Atc getTunedAtc() {
//    return this.atc;
//  }
//
//  public boolean hasRadarContact() {
//    return secondsWithoutRadarContact == 0;
//  }
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
//
//  public int getSecondsWithoutRadarContact() {
//    return this.secondsWithoutRadarContact;
//  }
//
//  public void init(Atc atc) {
//    assert atc != null;
//    this.atc = atc;
//  }
//
//  public void setHasRadarContact() {
//    this.secondsWithoutRadarContact = 0;
//  }
//
//  public void changeAtc(Atc atc) {
//    assert atc != null;
//    this.atc = atc;
//    this.secondsWithoutRadarContact = 1;
//  }
//}
