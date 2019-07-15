package eng.jAtcSim.lib.airplanes.pilots.modules;

import eng.jAtcSim.lib.airplanes.pilots.Pilot;
import eng.jAtcSim.lib.atcs.Atc;
import eng.jAtcSim.lib.atcs.TowerAtc;
import eng.jAtcSim.lib.global.Global;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.GoodDayNotification;

public class AtcModule {
  private final Pilot.Pilot5Module parent;
  private int altitudeOrderedByAtc;
  private Atc atc;
  private int secondsWithoutRadarContact = 0;

  public AtcModule(Pilot.Pilot5Module parent) {
    assert parent != null;
    this.parent = parent;
  }

  public Atc getTunedAtc() {
    return this.atc;
  }

  public boolean hasRadarContact() {
    return secondsWithoutRadarContact == 0;
  }

  public void requestRadarContactIfRequired() {
    if (this.secondsWithoutRadarContact > 0) {
      this.secondsWithoutRadarContact++;
      if (this.secondsWithoutRadarContact % Global.REPEATED_RADAR_CONTACT_REQUEST_SECONDS == 0) {
        parent.say(
            new GoodDayNotification(
                parent.getPlane().getFlight().getCallsign(),
                parent.getPlane().getSha().getAltitude(),
                parent.getPlane().getSha().getTargetAltitude(),
                parent.getPlane().getEmergencyModule().isEmergency(),
                true));
      }
    }
  }

  public int getSecondsWithoutRadarContact() {
    return this.secondsWithoutRadarContact;
  }

  public void init(Atc atc) {
    assert atc != null;
    this.atc = atc;
  }

  public void setHasRadarContact() {
    this.secondsWithoutRadarContact = 0;
  }

  public void changeAtc(Atc atc) {
    assert atc != null;
    this.atc = atc;
    this.secondsWithoutRadarContact = 1;
  }
}
