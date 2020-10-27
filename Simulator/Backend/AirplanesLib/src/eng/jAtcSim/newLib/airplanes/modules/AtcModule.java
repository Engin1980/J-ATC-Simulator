package eng.jAtcSim.newLib.airplanes.modules;

import eng.eSystem.eXml.XElement;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.GoodDayNotification;
import eng.jAtcSimLib.xmlUtils.XmlSaveUtils;

public class AtcModule extends Module {
  private static final int REPEATED_RADAR_CONTACT_REQUEST_SECONDS = 45;
  private AtcId atcId;
  private int secondsWithoutRadarContact = 0;

  public AtcModule(Airplane plane, AtcId initialAtcId) {
    super(plane);
    EAssert.Argument.isNotNull(initialAtcId, "initialAtcId");
    this.atcId = initialAtcId;
  }

  public void changeAtc(AtcId atcId) {
    EAssert.Argument.isNotNull(atcId);
    this.atcId = atcId;
    this.secondsWithoutRadarContact = 1;
  }

  @Override
  public void elapseSecond() {
    int seconds = getAndIncreaseSecondsWithoutRadarContactIfRequired();
    if (seconds > 0 && seconds % AtcModule.REPEATED_RADAR_CONTACT_REQUEST_SECONDS == 0) {
      wrt.sendMessage(
              new GoodDayNotification(
                      rdr.getCallsign(),
                      rdr.getSha().getAltitude(),
                      rdr.getSha().getTargetAltitude(),
                      rdr.isEmergency(),
                      true));
    }
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

  public void save(XElement target) {
    XmlSaveUtils.Field.storeField(target, this, "atcId", (AtcId q) -> q.getName());
    XmlSaveUtils.Field.storeField(target, this, "secondsWithoutRadarContact");
  }

  public void setHasRadarContact() {
    this.secondsWithoutRadarContact = 0;
  }

  private int getAndIncreaseSecondsWithoutRadarContactIfRequired() {
    if (secondsWithoutRadarContact != 0)
      secondsWithoutRadarContact++;
    return secondsWithoutRadarContact;
  }
}
