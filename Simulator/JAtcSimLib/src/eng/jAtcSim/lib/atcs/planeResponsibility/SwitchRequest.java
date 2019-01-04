package eng.jAtcSim.lib.atcs.planeResponsibility;

import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.atcs.Atc;
import eng.jAtcSim.lib.global.ETime;

public class SwitchRequest {
  private ETime creationTime;
  private Atc atc;
  private ETime repeatRequestTime;
  private ETime confirmedTime = null;
  private SwitchRoutingRequest routing;

  public void setConfirmed(SwitchRoutingRequest newRoutingIfRequired){
    this.confirmedTime = Acc.now().clone();
    this.routing = newRoutingIfRequired;
  }

  public SwitchRequest(Atc atc) {
    this.atc = atc;
    this.creationTime = Acc.now().clone();
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

  public Atc getAtc() {
    return atc;
  }

  public void reset() {
    this.confirmedTime = null;
    this.routing = null;
  }

  void confirm(Atc oldAtc) {
    this.atc = oldAtc;
  }

  public ETime getConfirmedTime() {
    return confirmedTime;
  }

  public boolean isConfirmed(){
    return this.confirmedTime != null;
  }

  public void updateLastRequestTime(){
    this.repeatRequestTime = Acc.now().addSeconds(30);
  }
}
