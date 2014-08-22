/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.commands;

import jatcsimlib.exceptions.ENotSupportedException;
import jatcsimlib.exceptions.ERuntimeException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Marek
 */
public class CommandFormat {

  private static final List<ParseDef> lst;

  static {
    lst = new ArrayList<>();
    lst.add(new ParseDef(ChangeHeadingCommand.class, "((FH)|(TL)|(TR)) ?(\\d{1,3})", "FH", "TR", "TL"));
    lst.add(new ParseDef(ChangeAltitudeCommand.class, "((MA)|(CM)|(DM)) ?(\\d{1,3})", "MA", "CM", "DM"));
    lst.add(new ParseDef(ChangeSpeedCommand.class, "((SU)|(SD) ?(\\d{3})", "SU", "SD"));
  }

  public static Command parse(String line) {
    ParseDef pd = getRegexGrouper(line);
    RegexGrouper rg = RegexGrouper.apply(line, pd.pattern);

    Method m = tryGetParseCommandMethodToInvoke(pd.commandType);
    Command ret;
    try {
      ret = (Command) m.invoke(null, rg);
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
      throw new ERuntimeException("???");
    }
    return ret;
  }

  private static Method tryGetParseCommandMethodToInvoke(Class<? extends Command> commandType) {
    Method ret;
    try {
      ret = CommandFormat.class.getMethod("parseCommand", commandType);
    } catch (NoSuchMethodException | SecurityException ex) {
      ret = null;
    }

    return ret;
  }

  private static ParseDef getRegexGrouper(String line) {
    line = line.toUpperCase();
    ParseDef pd = null;
    for (ParseDef tmp : lst) {
      for (String pref : tmp.prefixes) {
        if (line.startsWith(pref)) {
          return tmp;
        }
      }
      if (pd != null) {
        break;
      }
    }

    return null;
  }

  private static Command parseCommand(ChangeHeadingCommand cmd, RegexGrouper rg) {
    ChangeHeadingCommand.eDirection d;
    switch (rg.getString(1)) {
      case "FH":
        d = ChangeHeadingCommand.eDirection.any;
        break;
      case "TL":
        d = ChangeHeadingCommand.eDirection.left;
        break;
      case "TR":
        d = ChangeHeadingCommand.eDirection.right;
        break;
      default:
        throw new ENotSupportedException();
    }
    int h = rg.getInt(5);
    ChangeHeadingCommand ret = new ChangeHeadingCommand(h, d);
    return ret;
  }

  private static Command parseCommand(ChangeAltitudeCommand cmd, RegexGrouper rg) {
    ChangeAltitudeCommand ret;
    ChangeAltitudeCommand.eDirection d;
    int a;

    switch (rg.getString(1)) {
      case "MA":
        d = ChangeAltitudeCommand.eDirection.any;
        break;
      case "CM":
        d = ChangeAltitudeCommand.eDirection.climb;
        break;
      case "DM":
        d = ChangeAltitudeCommand.eDirection.descend;
        break;
      default:
        throw new ERuntimeException("Invalid prefix for Maintain-altitude command.");
    }

    a = rg.getInt(5) * 100;

    ret = new ChangeAltitudeCommand(d, a);
    return ret;
  }

  private static Command parseCommand(ChangeSpeedCommand cmpd, RegexGrouper rg) {
    ChangeSpeedCommand.eDirection d;
    int s;

    switch (rg.getString(1)) {
      case "SU":
        d = ChangeSpeedCommand.eDirection.increase;
        break;
      case "SD":
        d = ChangeSpeedCommand.eDirection.decrease;
        break;
      default:
        throw new ERuntimeException("Invalid prefix");
    }
    s = rg.getInt(2);

    ChangeSpeedCommand ret = new ChangeSpeedCommand(d, s);
    return ret;
  }

  public String format(Command cmd) {
    Method m;
    m = tryGetFormatCommandMethodToInvoke(cmd.getClass());

    String ret;
    try {
      ret = (String) m.invoke(null, cmd);
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
      throw new ERuntimeException("Format-command failed for " + cmd.getClass());
    }
    return ret;
  }

  private Method tryGetFormatCommandMethodToInvoke(Class<? extends Command> commandType) {
    Method ret;
    try {
      ret = CommandFormat.class.getMethod("formatCommand", commandType);
    } catch (NoSuchMethodException | SecurityException ex) {
      ret = null;
    }

    return ret;
  }

  private String formatCommand(ChangeHeadingCommand cmd) {
    StringBuilder sb = new StringBuilder();
    switch (cmd.getDirection()){
      case any:
        sb.append("FH");
        break;
      case left:
        sb.append("TL");
        break;
      case right:
        sb.append("TR");
        break;
      default:
        throw new ENotSupportedException();
    }
    sb.append(String.format("%03d", cmd.getHeading()));
    return sb.toString();
  }
}

class ParseDef {

  public final String[] prefixes;
  public final String pattern;
  public final Class commandType;

  public ParseDef(Class commandType, String pattern, String... prefixes) {
    this.prefixes = prefixes;
    this.pattern = pattern;
    this.commandType = commandType;
  }
}
