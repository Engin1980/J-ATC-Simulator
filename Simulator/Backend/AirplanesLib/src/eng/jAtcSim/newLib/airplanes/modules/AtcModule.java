package eng.jAtcSim.newLib.airplanes.modules;

import eng.eSystem.eXml.XElement;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.GoodDayNotification;
import eng.newXmlUtils.annotations.XmlConstructor;
import exml.XContext;

public class AtcModule extends Module {
  private static final int REPEATED_RADAR_CONTACT_REQUEST_SECONDS = 45;
  private AtcId atcId;
  private int secondsWithoutRadarContact = 0;

  @XmlConstructor
  private AtcModule() {
    super(null);
  }

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

  public void setHasRadarContact() {
    this.secondsWithoutRadarContact = 0;
  }

  private int getAndIncreaseSecondsWithoutRadarContactIfRequired() {
    if (secondsWithoutRadarContact != 0)
      secondsWithoutRadarContact++;
    return secondsWithoutRadarContact;
  }

  @Override
  public void save(XElement elm, XContext ctx) {
    super.save(elm, ctx);
    ctx.saver.saveRemainingFields(this, elm);
  }

  @Override
  public void load(XElement elm, XContext ctx) {
    super.load(elm, ctx);
  }
}
