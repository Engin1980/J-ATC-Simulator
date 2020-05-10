package eng.jAtcSim.newLib.textProcessing.implemented.atcFormatter.formatters;

import eng.eSystem.EStringBuilder;
import eng.jAtcSim.newLib.speeches.atc.user2atc.PlaneSwitchRequest;
import eng.jAtcSim.newLib.textProcessing.implemented.formatterHelpers.SmartTextSpeechFormatter;

public class PlaneSwitchRequestFormatter extends SmartTextSpeechFormatter<PlaneSwitchRequest> {
  @Override
  public String _format(PlaneSwitchRequest input) {
    String ret;
    if (input.getRunwayName() == null && input.getRouteName() == null) {
      EStringBuilder sb = new EStringBuilder();
      sb.appendFormat("%s ", input.getSquawk());
      if (input.getRunwayName() != null)
        sb.appendFormat("%s", input.getRunwayName());
      if (input.getRouteName() != null)
        sb.appendFormat("/%s", input.getRouteName());
      ret = sb.toString();
    } else
      ret = input.getSquawk().toString();
    return ret;
  }
}
