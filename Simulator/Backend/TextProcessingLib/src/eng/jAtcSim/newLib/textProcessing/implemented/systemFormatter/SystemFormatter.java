package eng.jAtcSim.newLib.textProcessing.implemented.systemFormatter;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.speeches.system.ISystemNotification;
import eng.jAtcSim.newLib.speeches.system.system2user.SystemRejection;
import eng.jAtcSim.newLib.textProcessing.formatting.ISystemFormatter;
import eng.jAtcSim.newLib.textProcessing.implemented.formatterHelpers.TextSpeechFormatter;
import eng.jAtcSim.newLib.textProcessing.implemented.formatterHelpers.TextSpeechFormatterList;
import eng.jAtcSim.newLib.textProcessing.implemented.systemFormatter.formatters.CurrentTickNotificationFormatter;
import eng.jAtcSim.newLib.textProcessing.implemented.systemFormatter.formatters.MetarNotificationFormatter;
import eng.jAtcSim.newLib.textProcessing.implemented.systemFormatter.formatters.StringMessageFormatter;

public class SystemFormatter implements ISystemFormatter<String> {

  private static final TextSpeechFormatterList<ISystemNotification> formatters;

  static {
    formatters = new TextSpeechFormatterList<>();
    formatters.add(new CurrentTickNotificationFormatter());
    formatters.add(new MetarNotificationFormatter());
    formatters.add(new StringMessageFormatter());
  }

  @Override
  public String format(ISystemNotification input) {
    String ret;
    if (input instanceof SystemRejection)
      ret = formatRejection((SystemRejection) input);
    else
      ret = formatNormalNotification(input);
    return ret;
  }

  private String formatNormalNotification(ISystemNotification input) {
    TextSpeechFormatter<? extends ISystemNotification> fmt = formatters.tryGet(input);
    EAssert.isNotNull(fmt);
    String ret = fmt.format(input);
    return ret;
  }

  private String formatRejection(SystemRejection input) {
    return "Unable to process system command. " + input.getReason();
  }
}
