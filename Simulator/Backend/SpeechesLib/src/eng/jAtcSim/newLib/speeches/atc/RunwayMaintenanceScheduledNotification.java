package eng.jAtcSim.newLib.speeches.atc;

import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;

public class RunwayMaintenanceScheduledNotification extends RunwayMaintenanceBaseNotification {
  private final EDayTimeStamp maintenanceStartTime;
  private final int maintenanceDurationInMinutes;

  public RunwayMaintenanceScheduledNotification(String runwayName, EDayTimeStamp maintenanceStartTime, int maintenanceDurationInMinutes) {
    super(runwayName);
    this.maintenanceStartTime = maintenanceStartTime;
    this.maintenanceDurationInMinutes = maintenanceDurationInMinutes;
  }

  public EDayTimeStamp getMaintenanceStartTime() {
    return maintenanceStartTime;
  }

  public int getMaintenanceDurationInMinutes() {
    return maintenanceDurationInMinutes;
  }
}
