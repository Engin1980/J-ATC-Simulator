package eng.jAtcSim.newLib.textProcessing.implemented.atcFormatter.formatters;

import eng.jAtcSim.newLib.speeches.atc.atc2user.RunwayMaintenanceProceedingNotification;
import eng.jAtcSim.newLib.textProcessing.implemented.formatterHelpers.SmartTextSpeechFormatter;

public class RunwayMaintenanceProceedingNotificationFormatter extends
    SmartTextSpeechFormatter<RunwayMaintenanceProceedingNotification> {
  @Override
  public String _format(RunwayMaintenanceProceedingNotification input) {
    return String.format(
        "The runway %s is now under maintenance. The maintenance expected end time is at %s.",
        input.getRunwayName(),
        input.getExpectedMaintenanceEndTime().toHourMinuteString()
    );
  }
}
