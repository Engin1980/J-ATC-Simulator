/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.commands.formatting;

import jatcsimlib.commands.Command;
import jatcsimlib.commands.CommandList;
import jatcsimlib.exceptions.ERuntimeException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Marek Vajgl
 */
public class Formatters {

  private Formatters() {
  }

  /**
   * Invokes required function by reflection or throws exception.
   *
   * @param cmd
   * @param fmt
   * @return
   */
  public static String format(Command cmd, Formatter fmt) {
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

  public static String format(CommandList commands, Formatter fmt) {
    if (commands.size() == 1) {
      String s = format(commands.get(0), fmt);
      s = Character.toUpperCase(s.charAt(0)) + s.substring(1) + ".";
      return s;
    } else {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < commands.size(); i++) {
        String s = Formatters.format(commands.get(i), LongFormatter.getInstance());
        if (i == 0)
          sb.append(Character.toUpperCase(s.charAt(0))).append(s.substring(1));
          //sb.append(s);
        else
          sb.append("; ").append(s);
      }
      return sb.toString();
    }
  }

  private static Method tryGetFormatCommandMethodToInvoke(Class<? extends Command> commandType) {
    Method ret;
    try {
      ret = Formatter.class.getDeclaredMethod("format", commandType);
    } catch (NoSuchMethodException | SecurityException ex) {
      ret = null;
    }

    return ret;
  }
}
