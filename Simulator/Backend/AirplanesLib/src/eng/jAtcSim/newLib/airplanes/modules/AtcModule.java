package eng.jAtcSim.newLib.airplanes.modules;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.accessors.IPlaneInterface;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.speeches.airplane2atc.GoodDayNotification;

public class AtcModule extends Module {
  private static final int REPEATED_RADAR_CONTACT_REQUEST_SECONDS = 45;
  private AtcId atcId;
  private int secondsWithoutRadarContact = 0;

  public AtcModule(IPlaneInterface plane) {
    super(plane);
  }

  @Override
  public void elapseSecond() {
      int seconds = getAndIncreaseSecondsWithoutRadarContactIfRequired();
      if (seconds % AtcModule.REPEATED_RADAR_CONTACT_REQUEST_SECONDS == 0){
        plane.sendMessage(
            new GoodDayNotification(
                plane.getCallsign(),
                plane.getAltitude(),
                plane.getTargetAltitude(),
                plane.isEmergency(),
                true));
      }
  }

  public void changeAtc(AtcId atcId) {
    EAssert.Argument.isNotNull(atcId);
    this.atcId = atcId;
    this.secondsWithoutRadarContact = 1;
  }

  private int getAndIncreaseSecondsWithoutRadarContactIfRequired() {
    if (secondsWithoutRadarContact != 0)
      secondsWithoutRadarContact++;
    return secondsWithoutRadarContact;
  }

  public int getSecondsWithoutRadarContact() {
    return this.secondsWithoutRadarContact;
  }

  public AtcId getTunedAtc() {
    return this.atcId;
  }

  public boolean hasRadarContact() {
    return secondsWithoutRadarContact == 0;
  }

  public void setHasRadarContact() {
    this.secondsWithoutRadarContact = 0;
  }
}
