package eng.jAtcSim.newLib.speeches.atc.atc2user;

import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;

public class RunwayMaintenanceProceedingNotification extends RunwayMaintenanceBaseNotification {
  private final EDayTimeStamp expectedMaintenanceEndTime;

  public RunwayMaintenanceProceedingNotification(String runwayName, EDayTimeStamp expectedMaintenanceEndTime) {
    super(runwayName);
    this.expectedMaintenanceEndTime = expectedMaintenanceEndTime;
  }

  public EDayTimeStamp getExpectedMaintenanceEndTime() {
    return expectedMaintenanceEndTime;
  }

  @Override
  public String toString() {
    return super.toString();
  }
}
