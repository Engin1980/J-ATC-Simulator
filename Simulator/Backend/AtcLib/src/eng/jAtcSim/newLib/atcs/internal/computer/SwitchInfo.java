package eng.jAtcSim.newLib.atcs.internal.computer;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.atcs.contextLocal.Context;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.PostContracts;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;

import exml.IXPersistable;
import exml.annotations.XConstructor;

class SwitchInfo implements IXPersistable {
  private final AtcId atcId;
  private final EDayTimeStamp firstRequest;
  private EDayTimeStamp lastRequest;

  @XConstructor

  private SwitchInfo() {
    atcId = null;
    firstRequest = null;
    PostContracts.register(this, () -> this.atcId != null);
  }

  public SwitchInfo(AtcId otherAtcId) {
    EAssert.Argument.isNotNull(otherAtcId, "otherAtcId");
    this.atcId = otherAtcId;
    this.firstRequest = Context.getShared().getNow().toStamp();
    this.lastRequest = this.firstRequest;
  }

  public AtcId getAtcId() {
    return atcId;
  }

  public EDayTimeStamp getFirstRequest() {
    return firstRequest;
  }

  public EDayTimeStamp getLastRequest() {
    return lastRequest;
  }

  public void setLastRequest(EDayTimeStamp lastRequest) {
    EAssert.Argument.isNotNull(lastRequest, "lastRequest");
    EAssert.Argument.isTrue(lastRequest.isAfter(this.lastRequest));
    this.lastRequest = lastRequest;
  }
}
