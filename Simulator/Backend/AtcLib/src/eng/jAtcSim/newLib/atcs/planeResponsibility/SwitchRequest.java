package eng.jAtcSim.newLib.atcs.planeResponsibility;

import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Global;
import eng.jAtcSim.newLib.shared.context.SharedAcc;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;

public class SwitchRequest {
  private final EDayTimeStamp creationTime;
  private AtcId atc;
  private EDayTimeStamp repeatRequestTime;
  private EDayTimeStamp confirmedTime = null;
  private SwitchRoutingRequest routing;

  public void setConfirmed(SwitchRoutingRequest newRoutingIfRequired) {
    this.confirmedTime = SharedAcc.getNow().toStamp();
    this.routing = newRoutingIfRequired;
  }

  public SwitchRequest(AtcId atcId) {
    this.atc = atcId;
    this.creationTime = SharedAcc.getNow().toStamp();
    this.updateLastRequestTime();
  }

  public SwitchRoutingRequest getRouting() {
    return routing;
  }

  public EDayTimeStamp getRepeatRequestTime() {
    return repeatRequestTime;
  }

  public EDayTimeStamp getCreationTime() {
    return creationTime;
  }

  public AtcId getAtc() {
    return atc;
  }

  public void reset() {
    this.confirmedTime = null;
    this.routing = null;
  }

  public void deleteConfirmedRouting() {
    this.routing = null;
  }

  void confirm(AtcId oldAtc) {
    this.atc = oldAtc;
  }

  public EDayTimeStamp getConfirmedTime() {
    return confirmedTime;
  }

  public boolean isConfirmed() {
    return this.confirmedTime != null;
  }

  public void updateLastRequestTime() {
    this.repeatRequestTime = SharedAcc.getNow().addSeconds(Global.REPEATED_RADAR_CONTACT_REQUEST_SECONDS);
  }
}
