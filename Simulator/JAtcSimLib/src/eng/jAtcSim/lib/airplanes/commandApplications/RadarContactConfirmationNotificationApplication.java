package eng.jAtcSim.lib.airplanes.commandApplications;

import eng.jAtcSim.lib.airplanes.interfaces.forPilot.IPilotWriteSimple;
import eng.jAtcSim.lib.speaking.fromAtc.notifications.RadarContactConfirmationNotification;

public class RadarContactConfirmationNotificationApplication extends NotificationApplication<RadarContactConfirmationNotification> {
  public ConfirmationResult confirm(IPilotWriteSimple pilot, RadarContactConfirmationNotification c) {
    return ConfirmationResult.getEmpty();
  }

  public ApplicationResult apply(IPilotWriteSimple pilot, RadarContactConfirmationNotification c) {
    pilot.processRadarContactConfirmation();
    return ApplicationResult.getEmpty();
  }
}
