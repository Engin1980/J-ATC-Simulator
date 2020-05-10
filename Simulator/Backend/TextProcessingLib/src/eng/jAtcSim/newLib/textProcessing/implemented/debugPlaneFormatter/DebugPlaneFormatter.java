package eng.jAtcSim.newLib.textProcessing.implemented.debugPlaneFormatter;

import eng.eSystem.collections.*;
import eng.jAtcSim.newLib.speeches.airplane.IPlaneSpeech;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.PlaneConfirmation;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.PlaneRejection;
import eng.jAtcSim.newLib.textProcessing.formatting.IPlaneFormatter;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class DebugPlaneFormatter implements IPlaneFormatter<String> {
  @Override
  public String format(IPlaneSpeech input) {
    String ret;
    if (input instanceof PlaneConfirmation)
      ret = formatConfirmation((PlaneConfirmation)input);
    else if (input instanceof PlaneRejection)
      ret = formatRejection((PlaneRejection) input);
    else
      ret = formatNormal(input);
    return ret;
  }

  private String formatNormal(IPlaneSpeech input) {
    todo tady todle dopsat nejak jednoduse at se s tim moc neseru.
    return null;
  }

  private String formatRejection(PlaneRejection input) {
    return sf("Rejection: '%s'. Reason: %s", format(input.getOrigin()), input.getReason());
  }

  private String formatConfirmation(PlaneConfirmation input) {
    return sf("Confirmation: '%s'.", format(input.getOrigin()));
  }
}
