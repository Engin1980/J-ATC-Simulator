package eng.jAtcSim.newLib.textProcessing.implemented.atcFormatter.formatters;

import eng.jAtcSim.newLib.speeches.atc.atc2user.RunwayMaintenanceEndedNotification;
import eng.jAtcSim.newLib.textProcessing.implemented.formatterHelpers.SmartTextSpeechFormatter;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class RunwayMaintenanceEndedNotificationFormatter extends SmartTextSpeechFormatter<RunwayMaintenanceEndedNotification> {
  @Override
  public String _format(RunwayMaintenanceEndedNotification input) {
    return sf(
        "Maintenance of runway %s has ended.",
        input.getRunwayName()
    );
  }
}
