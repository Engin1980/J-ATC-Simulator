package eng.jAtcSim.newLib.textProcessing.implemented.atcFormatter.formatters;

import eng.eSystem.EStringBuilder;
import eng.jAtcSim.newLib.speeches.atc.planeSwitching.PlaneSwitchRequest;
import eng.jAtcSim.newLib.textProcessing.implemented.formatterHelpers.SmartTextSpeechFormatter;

public class PlaneSwitchRequestFormatter extends SmartTextSpeechFormatter<PlaneSwitchRequest> {
  @Override
  public String _format(PlaneSwitchRequest input) {
    String ret;
    if (input.getRouting() != null) {
      EStringBuilder sb = new EStringBuilder();
      sb.appendFormat("%s ", input.getSquawk());
      if (input.getRouting().getRunwayThresholdName() != null)
        sb.appendFormat("%s", input.getRouting().getRunwayThresholdName());
      if (input.getRouting().getRouteName() != null)
        sb.appendFormat("/%s", input.getRouting().getRouteName());
      ret = sb.toString();
    } else
      ret = input.getSquawk().toString();
    if (input.isRepeated())
      ret += " (repeated)";
    return ret;
  }
}
