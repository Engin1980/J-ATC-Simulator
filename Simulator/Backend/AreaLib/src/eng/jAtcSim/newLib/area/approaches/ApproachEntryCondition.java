package eng.jAtcSim.newLib.area.approaches;

import eng.jAtcSim.newLib.area.approaches.conditions.ICondition;
import eng.jAtcSim.newLib.shared.PostContracts;
import exml.annotations.XConstructor;

public class ApproachEntryCondition {

  public enum ApproachRejectionReason {
    invalidHeading, invalidLocation, thresholdNotInSight, invalidAltitude
  }

  public static ApproachEntryCondition create(ICondition condition, ApproachRejectionReason rejectionReason) {
    return new ApproachEntryCondition(condition, rejectionReason);
  }

  private final ICondition entryCondition;
  private final ApproachRejectionReason rejectionReason;


  @XConstructor
  private ApproachEntryCondition() {
    entryCondition = null;
    rejectionReason = null;

    PostContracts.register(this, () -> this.entryCondition != null);
  }

  private ApproachEntryCondition(ICondition entryCondition, ApproachRejectionReason rejectionReason) {
    this.entryCondition = entryCondition;
    this.rejectionReason = rejectionReason;
  }

  public ICondition getEntryCondition() {
    return entryCondition;
  }

  public ApproachRejectionReason getRejectionReason() {
    return rejectionReason;
  }
}
