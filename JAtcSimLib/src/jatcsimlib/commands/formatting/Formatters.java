/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.commands.formatting;

import jatcsimlib.commands.Command;
import jatcsimlib.exceptions.ERuntimeException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *
 * @author Marek Vajgl
 */
public class Formatters {
  private Formatters (){}
  
  /**
   * Invokes required function by reflection or throws exception.
   * @param cmd
   * @param fmt
   * @return 
   */
  public static String format(Command cmd, Formatter fmt){
   Method m;
    m = tryGetFormatCommandMethodToInvoke(cmd.getClass());

    if (m == null) {
      throw new ERuntimeException("No \"format\" method found for type " + cmd.getClass().getSimpleName());
    }

    String ret;
    try {
      ret = (String) m.invoke(fmt, cmd);
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
      throw new ERuntimeException(
        String.format("Format-command invoke failed for class %s and parameter %s.",
          fmt.getClass().getName(),
          cmd.getClass().getName()));
    }
    return ret; 
  }
  
    private static Method tryGetFormatCommandMethodToInvoke(Class<? extends Command> commandType) {
    Method ret;
    try {
      ret = Formatter.class.getDeclaredMethod("format", commandType, boolean.class);
    } catch (NoSuchMethodException | SecurityException ex) {
      ret = null;
    }

    return ret;
  }
}
