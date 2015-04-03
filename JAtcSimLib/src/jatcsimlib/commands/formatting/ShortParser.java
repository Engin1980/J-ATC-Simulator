/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.commands.formatting;

import jatcsimlib.Acc;
import jatcsimlib.atcs.Atc;
import jatcsimlib.commands.*;
import jatcsimlib.exceptions.EInvalidCommandException;
import jatcsimlib.exceptions.ENotSupportedException;
import jatcsimlib.exceptions.ERuntimeException;
import jatcsimlib.global.EStringBuilder;
import jatcsimlib.global.SpeedRestriction;
import jatcsimlib.world.Approach;
import jatcsimlib.world.Navaid;
import jatcsimlib.world.PublishedHold;
import jatcsimlib.world.RunwayThreshold;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Marek Vajgl
 */
public class ShortParser implements Parser {

  // <editor-fold defaultstate="collapsed" desc=" Parsers static init ">
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
    parsers.add(new ShortcutCmdParser());
    parsers.add(new HoldCmdParser());

    parsers.add(new ClearedToApproachCmdParser());

    parsers.add(new ContactCmdParser());

    parsers.add(new ThenCmdParser());
  }
// </editor-fold>

  @Override
  public Command parseOne(String line) {
    line = normalizeCommandsInString(line);
    return parseMulti(line).get(0);
  }

  @Override
  public CommandList parseMulti(String line) {
    line = normalizeCommandsInString(line);
    CommandList ret = new CommandList();
    String tmp = line;
    while (tmp != null && tmp.length() > 0) {
      CmdParser p = getCmdParser(tmp);

      if (p == null) {
        throw new EInvalidCommandException("Failed to parse command prefix.",
          line.substring(0, line.length() - tmp.length() - 1),
          tmp);
      }

      RegexGrouper rg = RegexGrouper.apply(tmp, p.getPattern());

      if (rg == null) {
        throw new EInvalidCommandException("Failed to parse command. Probably invalid syntax?",
          line.substring(0, line.length() - tmp.length() - 1),
          tmp);
      }

      Command cmd = p.parse(rg);
      ret.add(cmd);
      tmp = tmp.substring(rg.getIndexOfCharacterAfterMatch()).trim();
    }

    return ret;
  }

  private static String normalizeCommandsInString(String line) {
    if (line == null || line.isEmpty()) {
      return "";
    }
    line = line.trim();
    while (line.contains("  ")) {
      line = line.replace("  ", " ");
    }
    return line.toUpperCase() + " ";
  }

  private static CmdParser getCmdParser(String line) {
    line = line.toUpperCase();
    for (CmdParser tmp : parsers) {
      for (String pref : tmp.getPrefixes()) {
        if (line.startsWith(pref)) {
          return tmp;
        }
      }
    }

    return null;
  }
}

// <editor-fold defaultstate="collapsed" desc=" CmdParser + implementations ">
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
  private static final String pattern = "((FH)|(TR)|(TL)) ?(\\d{1,3})?";

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
    ChangeHeadingCommand ret;

    if (rg.getString(5) == null) {
      ret = new ChangeHeadingCommand();
    } else {
      int h = rg.getInt(5);
      ret = new ChangeHeadingCommand(h, d);
    }
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

  private static final String[] prefixes = new String[]{"SM", "SL", "SE", "SR"};
  private static final String pattern = "(SR)|(?:(S[MLE]) ?(\\d{3}))";

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

    ChangeSpeedCommand ret;

    // 1. rg je SR
    // 2. rg je SL/SM/SE
    // 3. rg je kts
    if (rg.getString(1) != null) {
      ret = new ChangeSpeedCommand();
    } else {
      int speed = rg.getInt(3);
      char c = rg.getString(2).charAt(1);
      switch (c) {
        case 'L':
          ret = new ChangeSpeedCommand(SpeedRestriction.eDirection.atLeast, speed);
          break;
        case 'M':
          ret = new ChangeSpeedCommand(SpeedRestriction.eDirection.atMost, speed);
          break;
        case 'E':
          ret = new ChangeSpeedCommand(SpeedRestriction.eDirection.exactly, speed);
          break;
        default:
          throw new ENotSupportedException();
      }
    }
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
    Navaid n = Acc.area().getNavaids().tryGet(ns);
    if (n == null) {
      throw new EInvalidCommandException("Unable to find navaid named \"" + ns + "\".", rg.getMatch());
    }
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

  private static final String[] prefixes = new String[]{"C "};
  private static final String pattern = "C (I|II|III|G|V|R) (\\S+)";

  @Override
  public String getHelp() {
    EStringBuilder sb = new EStringBuilder();

    sb.appendLine("Cleared to approach");
    sb.appendLine("\t " + pattern);
    sb.appendLine("\tI\t.. ILS cat I");
    sb.appendLine("\tII\t.. ILS cat II");
    sb.appendLine("\tIII\t.. ILS cat III");
    sb.appendLine("\tR\t.. VOR/DME");
    sb.appendLine("\tG\t.. GPS");
    sb.appendLine("\tV\t.. visual");
    sb.appendLine("Example:");
    sb.appendLine("\t C I 24 \t - cleared ILS category I 24");
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
    String typeS = rg.getString(1);
    String runwayName = rg.getString(2);

    Approach.eType type;
    switch (typeS) {
      case "G":
        type = Approach.eType.GNSS;
        break;
      case "I":
        type = Approach.eType.ILS_I;
        break;
      case "II":
        type = Approach.eType.ILS_II;
        break;
      case "III":
        type = Approach.eType.ILS_III;
        break;
      case "N":
        type = Approach.eType.NDB;
        break;
      case "R":
        type = Approach.eType.VORDME;
        break;
      case "V":
        type = Approach.eType.Visual;
        break;
      default:
        throw new ENotSupportedException();
    }

    RunwayThreshold rt = Acc.airport().tryGetRunwayThreshold(runwayName);
    if (rt == null) {
      throw new EInvalidCommandException(
        "Cannot be cleared to approach. There is no runway name \"" + runwayName + "\".", rg.getMatch());
    }

    Approach app = rt.tryGetApproachByTypeWithILSDerived(type);
    if (app == null) {
      throw new EInvalidCommandException(
        "Cannot be cleared to approach. There is no approach type "
        + type + " for runway " + rt.getName(),
        rg.getMatch());
    }

    Command ret = new ClearedToApproachCommand(app);
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

    Navaid n = Acc.area().getNavaids().tryGet(ns);
    if (n == null) {
      throw new EInvalidCommandException("Unable to find navaid named \"" + ns + "\".", rg.getMatch());
    }
    Command ret = new ProceedDirectCommand(n);
    return ret;
  }
}

class ShortcutCmdParser extends CmdParser {

  private static final String[] prefixes = new String[]{"SH"};
  private static final String pattern = "SH (\\S+)";

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

    Navaid n = Acc.area().getNavaids().tryGet(ns);
    if (n == null) {
      throw new EInvalidCommandException("Unable to find navaid named \"" + ns + "\".", rg.getMatch());
    }
    Command ret = new ProceedDirectCommand(n);
    return ret;
  }
}

class HoldCmdParser extends CmdParser {

  private static final String[] prefixes = new String[]{"H"};
  private static final String pattern = "H (\\S{1,5})( (\\d{3}))?( (R|L))?";

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
    HoldCommand ret;

    String ns = rg.getString(1);
    Navaid n = Acc.area().getNavaids().tryGet(ns);
    if (n == null) {
      throw new EInvalidCommandException("Unable to find navaid named \"" + ns + "\".", rg.getMatch());
    }

    Integer heading = rg.tryGetInt(3);
    String leftOrRight = rg.tryGetString(5);

    if (heading == null) {
      PublishedHold h = Acc.airport().getHolds().get(n);

      if (h == null) {
        throw new EInvalidCommandException(
          "Hold over fix " + ns + " is not published. You must specify exact hold procedure.",
          rg.getMatch());
      }

      ret = new HoldCommand(h);
    } else {
      boolean left = leftOrRight.equals("L");
      ret = new HoldCommand(n, heading, left);
    }
    return ret;
  }
}

// </editor-fold>