package eng.jAtcSim.lib.airplanes.commandApplications;

import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.speaking.fromAtc.notifications.RadarContactConfirmationNotification;

public class RadarContactConfirmationNotificationApplication extends NotificationApplication<RadarContactConfirmationNotification> {
  public ConfirmationResult confirm(Airplane.Airplane4Command plane, RadarContactConfirmationNotification c) {
    return ConfirmationResult.getEmpty();
  }

  public ApplicationResult apply(Airplane.Airplane4Command plane, RadarContactConfirmationNotification c) {
    plane.getPilot().setHasRadarContact();
    return ApplicationResult.getEmpty();
  }
}
