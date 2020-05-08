package eng.jAtcSim.newLib.speeches.atc;

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
}
