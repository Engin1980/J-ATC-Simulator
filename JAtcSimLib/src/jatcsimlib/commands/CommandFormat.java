/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.commands;

import jatcsimlib.atcs.Atc;
import jatcsimlib.exceptions.EInvalidCommandException;
import jatcsimlib.exceptions.ENotSupportedException;
import jatcsimlib.exceptions.ERuntimeException;
import jatcsimlib.global.EStringBuilder;
import jatcsimlib.world.Navaid;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Marek
 */
public class CommandFormat {

  private static final List<CmdParser> parsers;

  static {
    parsers = new ArrayList<>();
    parsers.add(new ChangeHeadingCmdParser());
    parsers.add(new ChangeAltitudeCmdParser());
    parsers.add(new ChangeSpeedCmdParser());

    parsers.add(new AfterAltitudeCmdParser());
    parsers.add(new AfterSpeedCmdParser());
    parsers.add(new AfterNavaidCmdParser());

    parsers.add(new ProceedDirectCmdParser());

    parsers.add(new ContactCmdParser());

    parsers.add(new ThenCmdParser());
  }

  public static Command parseOne(String line) {
    line = normalizeCommandsInString(line);
    return parseMulti(line)[0];
  }

  public static Command[] parseMulti(String line) {
    line = normalizeCommandsInString(line);    
    List<Command> lst = new ArrayList<>();
    String tmp = line;
    while (tmp != null && tmp.length() > 0) {
      CmdParser p = getCmdParser(tmp);
      
      if (p == null){
        throw new EInvalidCommandException("Failed to parse command prefix.", 
            line.substring(0, line.length() -  tmp.length() - 1),
            tmp);
      }
      
      RegexGrouper rg = RegexGrouper.apply(tmp, p.getPattern());

      if (rg == null){
        throw new EInvalidCommandException("Failed to parse command. Probably invalid syntax?", 
            line.substring(0, line.length() -  tmp.length() - 1),
            tmp);
      }
      
      Command cmd = p.parse(rg);
      lst.add(cmd);
      tmp = tmp.substring(rg.getIndexOfCharacterAfterMatch()).trim();
    }

    Command[] ret = new Command[0];
    ret = lst.toArray(ret);
    return ret;
  }
  
  private static String normalizeCommandsInString(String line){
    return line.toUpperCase() + " ";
  }

  private static CmdParser getCmdParser(String line) {
    line = line.toUpperCase();
    CmdParser pd = null;
    for (CmdParser tmp : parsers) {
      for (String pref : tmp.getPrefixes()) {
        if (line.startsWith(pref)) {
          return tmp;
        }
      }
    }

    return null;
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
    switch (cmd.getDirection()) {
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

class CmdParserResult {

  public final Command command;
  public final String restOfLine;

  public CmdParserResult(Command command, String restOfLine) {
    this.command = command;
    this.restOfLine = restOfLine;
  }
}

abstract class CmdParser {

  abstract String[] getPrefixes();

  abstract String getPattern();

  public String getHelp() {
    EStringBuilder sb = new EStringBuilder();
    sb.appendLine(this.getClass().getSimpleName());
    sb.appendLine("\t" + this.getPattern());
    return sb.toString();
  }

  abstract Command parse(RegexGrouper line);
}

class ChangeHeadingCmdParser extends CmdParser {

  private static final String[] prefixes = new String[]{"FH", "TR", "TL"};
  private static final String pattern = "((FH)|(TL)|(TR)) ?(\\d{1,3})";

  @Override
  String[] getPrefixes() {
    return prefixes;
  }

  @Override
  String getPattern() {
    return pattern;
  }

  @Override
  Command parse(RegexGrouper rg) {
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

}

class ChangeAltitudeCmdParser extends CmdParser {

  private static final String[] prefixes = new String[]{"MA", "CM", "DM"};
  private static final String pattern = "((MA)|(CM)|(DM)) ?(\\d{1,3})";

  @Override
  String[] getPrefixes() {
    return prefixes;
  }

  @Override
  String getPattern() {
    return pattern;
  }

  @Override
  Command parse(RegexGrouper rg) {
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
}

class ChangeSpeedCmdParser extends CmdParser {

  private static final String[] prefixes = new String[]{"SU", "SD"};
  private static final String pattern = "((SU)|(SD) (\\d{3})";

  @Override
  String[] getPrefixes() {
    return prefixes;
  }

  @Override
  String getPattern() {
    return pattern;
  }

  @Override
  Command parse(RegexGrouper rg) {
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
}

class AfterAltitudeCmdParser extends CmdParser {

  private static final String[] prefixes = new String[]{"AA"};
  private static final String pattern = "AA (\\d{1,3})";

  @Override
  String[] getPrefixes() {
    return prefixes;
  }

  @Override
  String getPattern() {
    return pattern;
  }

  @Override
  Command parse(RegexGrouper rg) {
    int alt = rg.getInt(1) * 100;
    Command ret = new AfterAltitudeCommand(alt);
    return ret;
  }
}

class AfterSpeedCmdParser extends CmdParser {

  private static final String[] prefixes = new String[]{"AS"};
  private static final String pattern = "AS (\\d{1,3})";

  @Override
  String[] getPrefixes() {
    return prefixes;
  }

  @Override
  String getPattern() {
    return pattern;
  }

  @Override
  Command parse(RegexGrouper rg) {
    int s = rg.getInt(1);
    Command ret = new AfterSpeedCommand(s);
    return ret;
  }
}

class AfterNavaidCmdParser extends CmdParser {

  private static final String[] prefixes = new String[]{"AN"};
  private static final String pattern = "AN (\\S+)";

  @Override
  String[] getPrefixes() {
    return prefixes;
  }

  @Override
  String getPattern() {
    return pattern;
  }

  @Override
  Command parse(RegexGrouper rg) {
    String ns = rg.getString(1);
    Navaid n = null;
    Command ret = new AfterNavaidCommand(n);
    return ret;
  }
}

class ThenCmdParser extends CmdParser {

  private static final String[] prefixes = new String[]{"T"};
  private static final String pattern = "T";

  @Override
  String[] getPrefixes() {
    return prefixes;
  }

  @Override
  String getPattern() {
    return pattern;
  }

  @Override
  Command parse(RegexGrouper rg) {
    Command ret = new ThenCommand();
    return ret;
  }
}

class ContactCmdParser extends CmdParser {

  private static final String[] prefixes = new String[]{"CT", "CA", "CC"};
  private static final String pattern = "(CT|CA|CC)";

  @Override
  String[] getPrefixes() {
    return prefixes;
  }

  @Override
  String getPattern() {
    return pattern;
  }

  @Override
  Command parse(RegexGrouper rg) {
    Atc.eType t;
    switch (rg.getString(1)) {
      case "CT":
        t = Atc.eType.twr;
        break;
      case "CA":
        t = Atc.eType.app;
        break;
      case "CC":
        t = Atc.eType.ctr;
        break;
      default:
        throw new ENotSupportedException();
    }
    ContactCommand ret = new ContactCommand(t);
    return ret;
  }
}

class ClearedToApproachCmdParser extends CmdParser {

  private static final String[] prefixes = new String[]{"C"};
  private static final String pattern = "C (I|V|R) (\\S+)";

  @Override
  public String getHelp() {
    EStringBuilder sb = new EStringBuilder();

    sb.appendLine("Cleared to approach");
    sb.appendLine("\t " + pattern);
    sb.appendLine("\tI\t.. ILS");
    sb.appendLine("\tR\t.. VOR/DME");
    sb.appendLine("\tV\t.. visual");
    sb.appendLine("Example:");
    sb.appendLine("\t C I 24 \t - cleared ILS 24");
    sb.appendLine("\t C R 24 \t - cleared VOR/DME 24");
    sb.appendLine("\t C V 24 \t - cleared visual 24");

    return sb.toString();
  }

  @Override
  String[] getPrefixes() {
    return prefixes;
  }

  @Override
  String getPattern() {
    return pattern;
  }

  @Override
  Command parse(RegexGrouper rg) {
    String ns = rg.getString(1);
    Navaid n = null;
    Command ret = new AfterNavaidCommand(n);
    return ret;
  }
}

class ProceedDirectCmdParser extends CmdParser {

  private static final String[] prefixes = new String[]{"PD"};
  private static final String pattern = "PD (\\S+)";

  @Override
  String[] getPrefixes() {
    return prefixes;
  }

  @Override
  String getPattern() {
    return pattern;
  }

  @Override
  Command parse(RegexGrouper rg) {
    String ns = rg.getString(1);
    
    Navaid n = Navaid.area.getNavaids().tryGet(ns);
    if (n == null)
      throw new EInvalidCommandException("Unable to find navaid named \"" + ns + "\".",rg.getMatch());
    Command ret = new ProceedDirectCommand(n);
    return ret;
  }
}
