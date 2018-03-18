package eng.jAtcSim.lib.speaking.formatting;

import eng.jAtcSim.lib.atcs.Atc;
import eng.jAtcSim.lib.speaking.fromAtc.atc2atc.PlaneSwitchMessage;
import eng.jAtcSim.lib.exceptions.ERuntimeException;
import eng.jAtcSim.lib.speaking.ISpeech;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class Formatter {

  public String format(ISpeech speech) {
    String ret =
        formatByReflection(speech);
    return ret;
  }

  public String formatByReflection(ISpeech speech) {
    Formatter fmt = this;
    Method m;
    m = tryGetFormatCommandMethodToInvoke(speech.getClass());

    if (m == null) {
      throw new ERuntimeException(
          "No {format(...)} method found for type {%s} in the formatter of type {%s}.",
          speech.getClass().getName(),
          this.getClass().getName());
    }

    String ret;
    try {
      ret = (String) m.invoke(fmt, speech);
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
      throw new ERuntimeException(
          "Format-command invoke failed for class %s and parameter %s.",
          fmt.getClass().getName(),
          speech.getClass().getName());
    }
    return ret;
  }

  private Method tryGetFormatCommandMethodToInvoke(Class<? extends ISpeech> speechClass) {
    Method ret;
    try {
      ret = this.getClass().getDeclaredMethod("format", speechClass);
    } catch (NoSuchMethodException | SecurityException ex) {
      ret = null;
    }

    return ret;
  }

  public abstract String format(Atc sender, PlaneSwitchMessage msg);

}
