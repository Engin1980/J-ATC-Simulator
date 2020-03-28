package eng.jAtcSim.newLib.speeches.airplane2atc;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.speeches.INotification;

public class HighOrderedSpeedForApproach implements INotification {
  private int orderedSpeed;
  private int requiredSpeed;

  public int getOrderedSpeed() {
    return orderedSpeed;
  }

  public int getRequiredSpeed() {
    return requiredSpeed;
  }

  public HighOrderedSpeedForApproach(int orderedSpeed, int requiredSpeed) {
    EAssert.Argument.isTrue(orderedSpeed >= 0);
    EAssert.Argument.isTrue(requiredSpeed >= 0);
    this.orderedSpeed = orderedSpeed;
    this.requiredSpeed = requiredSpeed;
  }
}
