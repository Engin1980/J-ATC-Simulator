package eng.jAtcSim.newLib.atcs.planeResponsibility;

import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.GAcc;
import eng.jAtcSim.newLib.shared.time.ETime;

public class SwitchRequest {
  private ETime creationTime;
  private AtcId atc;
  private ETime repeatRequestTime;
  private ETime confirmedTime = null;
  private SwitchRoutingRequest routing;

  public void setConfirmed(SwitchRoutingRequest newRoutingIfRequired) {
    this.confirmedTime = GAcc.getNow().toStamp();
    this.routing = newRoutingIfRequired;
  }

  public SwitchRequest(AtcId atcId) {
    this.atc = atcId;
    this.creationTime = GAcc.getNow().toStamp();
    this.updateLastRequestTime();
  }

  public SwitchRoutingRequest getRouting() {
    return routing;
  }

  public ETime getRepeatRequestTime() {
    return repeatRequestTime;
  }

  public ETime getCreationTime() {
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

  public ETime getConfirmedTime() {
    return confirmedTime;
  }

  public boolean isConfirmed() {
    return this.confirmedTime != null;
  }

  public void updateLastRequestTime() {
    this.repeatRequestTime = GAcc.getNow().toStamp().addSeconds(30);
  }
}
