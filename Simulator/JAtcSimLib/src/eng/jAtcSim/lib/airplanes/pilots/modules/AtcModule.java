package eng.jAtcSim.lib.airplanes.pilots.modules;

import eng.jAtcSim.lib.airplanes.pilots.Pilot;
import eng.jAtcSim.lib.atcs.Atc;
import eng.jAtcSim.lib.global.Global;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.GoodDayNotification;

public class AtcModule {
  private final Pilot parent;
  private int altitudeOrderedByAtc;
  private Atc atc;
  private int secondsWithoutRadarContact;

  public AtcModule(Pilot parent) {
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
        this.say(
            new GoodDayNotification(
                this.parent.parent.getfli.getCallsign(), this.parent.getSha().getAltitude(), this.parent.getSha().getTargetAltitude(), this.parent.isEmergency(), true));
      }
    }
  }

  public int getSecondsWithoutRadarContact() {
  }
}
