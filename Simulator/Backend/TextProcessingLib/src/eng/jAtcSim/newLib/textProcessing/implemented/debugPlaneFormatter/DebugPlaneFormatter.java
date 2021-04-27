package eng.jAtcSim.newLib.textProcessing.implemented.debugPlaneFormatter;

import eng.eSystem.exceptions.ApplicationException;
import eng.jAtcSim.newLib.speeches.airplane.IPlaneSpeech;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.PlaneConfirmation;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.PlaneRejection;
import eng.jAtcSim.newLib.textProcessing.formatting.IPlaneFormatter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class DebugPlaneFormatter implements IPlaneFormatter<String> {

  private final FormatMethodImplementations fmt = new FormatMethodImplementations();

  @Override
  public String format(IPlaneSpeech input) {
    String ret;
    if (input instanceof PlaneConfirmation)
      ret = formatConfirmation((PlaneConfirmation) input);
    else if (input instanceof PlaneRejection)
      ret = formatRejection((PlaneRejection) input);
    else
      ret = formatNormal(input);
    return ret;
  }

  private String formatConfirmation(PlaneConfirmation input) {
    return sf("Confirmation: '%s'.", format(input.getOrigin()));
  }

  private String formatNormal(IPlaneSpeech input) {
    String ret = formatByReflection(input);
    return ret;
  }

  private String formatRejection(PlaneRejection input) {
    return sf("Rejection: '%s'. Reason: %s", format(input.getOrigin()), input.getReason());
  }

  private String formatByReflection(IPlaneSpeech speech) {
    Method m;
    m = tryGetFormatCommandMethodToInvoke(speech.getClass());

    if (m == null) {
      throw new ApplicationException(sf(
          "No {format(...)} method found for kind {%s} in the formatter of kind {%s}.",
          speech.getClass().getName(),
          this.getClass().getName()));
    }

    String ret;
    try {
      ret = (String) m.invoke(this.fmt, speech);
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
      throw new ApplicationException(sf(
          "Format-command invoke failed for class %s and parameter %s.",
          fmt.getClass().getName(),
          speech.getClass().getName()));
    }
    return ret;
  }

  private Method tryGetFormatCommandMethodToInvoke(Class<? extends IPlaneSpeech> speechClass) {
    Method ret;
    try {
      ret = fmt.getClass().getDeclaredMethod("format", speechClass);
    } catch (NoSuchMethodException | SecurityException ex) {
      ret = null;
    }

    return ret;
  }
}
