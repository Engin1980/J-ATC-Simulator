package jatcsimlib.speaking.formatting;

import jatcsimlib.exceptions.ERuntimeException;
import jatcsimlib.speaking.Speech;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class Formatter {

  public String format(Speech speech) {
    String ret =
        formatByReflection(speech);
    return ret;
  }

  public String formatByReflection(Speech speech) {
    Formatter fmt = this;
    Method m;
    m = tryGetFormatCommandMethodToInvoke(speech.getClass());

    if (m == null) {
      throw new ERuntimeException("No \"format\" method found for type " + speech.getClass().getSimpleName());
    }

    String ret;
    try {
      ret = (String) m.invoke(fmt, speech);
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
      throw new ERuntimeException(
          String.format("Format-command invoke failed for class %s and parameter %s.",
              fmt.getClass().getName(),
              speech.getClass().getName()));
    }
    return ret;
  }

  private Method tryGetFormatCommandMethodToInvoke(Class<? extends Speech> speechClass) {
    Method ret;
    try {
      ret = Formatter.class.getDeclaredMethod("format", speechClass);
    } catch (NoSuchMethodException | SecurityException ex) {
      ret = null;
    }

    return ret;
  }

}
