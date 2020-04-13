package eng.jAtcSim.newLib.airplanes.commandApplications;

import eng.jAtcSim.newLib.area.airplanes.interfaces.IAirplaneWriteSimple;
import eng.jAtcSim.newLib.area.speaking.fromAtc.notifications.RadarContactConfirmationNotification;

public class RadarContactConfirmationNotificationApplication extends NotificationApplication<RadarContactConfirmationNotification> {
  public ConfirmationResult confirm(IAirplaneCommand plane, RadarContactConfirmationNotification c) {
    return ConfirmationResult.getEmpty();
  }

  public ApplicationResult apply(IAirplaneCommand plane, RadarContactConfirmationNotification c) {
    plane.processRadarContactConfirmation();
    return ApplicationResult.getEmpty();
  }
}
