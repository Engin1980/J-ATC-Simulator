package eng.jAtcSim.newLib.atcs.internal.computer;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.atcs.contextLocal.Context;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Squawk;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;

class SwitchInfo {
  private final Squawk squawk;
  private final AtcId atcId;
  private final EDayTimeStamp firstRequest;
  private EDayTimeStamp lastRequest;

  public SwitchInfo(Squawk squawk, AtcId otherAtcId) {
    EAssert.Argument.isNotNull(squawk, "squawk");
    EAssert.Argument.isNotNull(otherAtcId, "otherAtcId");
    this.squawk = squawk;
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

  public Squawk getSqwk() {
    return squawk;
  }
}
