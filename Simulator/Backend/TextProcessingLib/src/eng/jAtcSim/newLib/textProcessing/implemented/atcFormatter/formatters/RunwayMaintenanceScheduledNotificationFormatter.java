package eng.jAtcSim.newLib.textProcessing.implemented.atcFormatter.formatters;

import eng.jAtcSim.newLib.speeches.atc.atc2user.RunwayMaintenanceScheduledNotification;
import eng.jAtcSim.newLib.textProcessing.implemented.formatterHelpers.SmartTextSpeechFormatter;

public class RunwayMaintenanceScheduledNotificationFormatter
    extends SmartTextSpeechFormatter<RunwayMaintenanceScheduledNotification> {
  @Override
  public String _format(RunwayMaintenanceScheduledNotification input) {
    return String.format(
        "Maintenance of the runway %s is scheduled at least at %s for approximately %d minutes.",
        input.getRunwayName(),
        input.getMaintenanceStartTime().toHourMinuteString(),
        input.getMaintenanceDurationInMinutes());
  }
}
