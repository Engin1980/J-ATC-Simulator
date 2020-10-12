package eng.jAtcSim.newLib.textProcessing.implemented.atcFormatter;

import eng.jAtcSim.newLib.speeches.atc.IAtcSpeech;
import eng.jAtcSim.newLib.textProcessing.formatting.IAtcFormatter;
import eng.jAtcSim.newLib.textProcessing.implemented.atcFormatter.formatters.*;
import eng.jAtcSim.newLib.textProcessing.implemented.formatterHelpers.TextSpeechFormatter;
import eng.jAtcSim.newLib.textProcessing.implemented.formatterHelpers.TextSpeechFormatterList;

public class AtcFormatter implements IAtcFormatter<String> {

  private static final TextSpeechFormatterList<IAtcSpeech> formatters;

  static{
    formatters = new TextSpeechFormatterList<>();
    formatters.add(new PlaneSwitchRequestFormatter());
//    formatters.add(new PlaneSwitchRequestCancelationFormatter());
    formatters.add(new RunwayMaintenanceEndedNotificationFormatter());
    formatters.add(new RunwayMaintenanceProceedingNotificationFormatter());
    formatters.add(new RunwayMaintenanceScheduledNotificationFormatter());
    formatters.add(new RunwayInUseNotificationFormatter());
  }

  @Override
  public String format(IAtcSpeech input) {
    TextSpeechFormatter<? extends IAtcSpeech> fmt = formatters.get(input);
    String ret = fmt.format(input);
    return ret;
  }
}
