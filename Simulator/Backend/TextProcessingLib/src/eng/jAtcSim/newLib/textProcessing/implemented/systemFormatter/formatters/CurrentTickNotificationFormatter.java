package eng.jAtcSim.newLib.textProcessing.implemented.systemFormatter.formatters;

import eng.jAtcSim.newLib.speeches.system.system2user.CurrentTickNotification;
import eng.jAtcSim.newLib.textProcessing.implemented.formatterHelpers.SmartTextSpeechFormatter;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class CurrentTickNotificationFormatter extends SmartTextSpeechFormatter<CurrentTickNotification> {
  @Override
  protected String _format(CurrentTickNotification input) {
    String ret;
    if (input.isChanged())
      ret = sf("Simulation second interval length changed to %d milliseconds.", input.getTickInterval());
    else
      ret = sf("Simulation second interval length is %d milliseconds.", input.getTickInterval());
    return ret;

  }
}
