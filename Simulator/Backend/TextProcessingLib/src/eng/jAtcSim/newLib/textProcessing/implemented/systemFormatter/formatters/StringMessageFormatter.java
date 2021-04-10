package eng.jAtcSim.newLib.textProcessing.implemented.systemFormatter.formatters;

import eng.jAtcSim.newLib.speeches.system.StringMessage;
import eng.jAtcSim.newLib.textProcessing.implemented.formatterHelpers.SmartTextSpeechFormatter;

public class StringMessageFormatter extends SmartTextSpeechFormatter<StringMessage> {
  @Override
  protected String _format(StringMessage input) {
    return input.getMessage();
  }
}
