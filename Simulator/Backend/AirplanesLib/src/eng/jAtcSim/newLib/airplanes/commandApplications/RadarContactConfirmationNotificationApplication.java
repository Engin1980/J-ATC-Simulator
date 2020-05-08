package eng.jAtcSim.newLib.airplanes.commandApplications;

import eng.jAtcSim.newLib.airplanes.internal.Airplane;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.RadarContactConfirmationNotification;

public class RadarContactConfirmationNotificationApplication extends NotificationApplication<RadarContactConfirmationNotification> {
  public ConfirmationResult confirm(Airplane plane, RadarContactConfirmationNotification c) {
    return ConfirmationResult.getEmpty();
  }

  public ApplicationResult apply(Airplane plane, RadarContactConfirmationNotification c) {
    plane.getWriter().processRadarContactConfirmation();
    return ApplicationResult.getEmpty();
  }
}
