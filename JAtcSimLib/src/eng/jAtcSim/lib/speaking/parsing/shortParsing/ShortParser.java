package eng.jAtcSim.lib.speaking.parsing.shortParsing;

import eng.eSystem.EStringBuilder;
import eng.jAtcSim.lib.exceptions.EInvalidCommandException;
import eng.jAtcSim.lib.speaking.IFromAtc;
import eng.jAtcSim.lib.speaking.ISpeech;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.parsing.Parser;

import java.util.ArrayList;
import java.util.List;

public class ShortParser extends Parser {

  // <editor-fold defaultstate="collapsed" desc=" Parsers static init ">
  private static final List<SpeechParser> parsers;

  static {
    parsers = new ArrayList<>();
    parsers.add(new ChangeHeadingParser());
    parsers.add(new ChangeAltitudeParser());
    parsers.add(new ChangeSpeedParser());

    parsers.add(new AfterAltitudeParser());
    parsers.add(new AfterSpeedParser());
    parsers.add(new AfterNavaidParser());

    parsers.add(new ProceedDirectParser());
    parsers.add(new ShortcutParser());
    parsers.add(new HoldParser());

    parsers.add(new ClearedToApproachParser());

    parsers.add(new ContactParser());

    parsers.add(new ThenParser());
    parsers.add(new RadarContactConfirmationParser());

    parsers.add(new GoAroundParser());

    parsers.add(new ReportDivertTimeParser());
    parsers.add(new DivertParser());
  }
// </editor-fold>

  @Override
  public ISpeech parseOne(String line) {
    line = normalizeCommandsInString(line);
    return parseMulti(line).get(0);
  }

  @Override
  public SpeechList<IFromAtc> parseMulti(String line) {
    line = normalizeCommandsInString(line);
    SpeechList ret = new SpeechList();
    String tmp = line;
    while (tmp != null && tmp.length() > 0) {
      SpeechParser p = getSpeechParser(tmp);

      if (p == null) {
        throw new EInvalidCommandException("Failed to parse command prefix.",
            line.substring(0, line.length() - tmp.length()),
            tmp);
      }

      RegexGrouper rg = RegexGrouper.apply(tmp, p.getPattern());

      if (rg == null) {
        throw new EInvalidCommandException("Failed to parse command. Probably invalid syntax?",
            line.substring(0, line.length() - tmp.length()),
            tmp);
      }

      ISpeech cmd = p.parse(rg);
      ret.add(cmd);
      tmp = tmp.substring(rg.getIndexOfCharacterAfterMatch() + 1);
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

  private static SpeechParser getSpeechParser(String line) {
    for (SpeechParser tmp : parsers) {
      for (String pref : tmp.getPrefixes()) {
        if (line.startsWith(pref+" ")) {
          return tmp;
        }
      }
    }

    return null;
  }

  @Override
  public String getHelp(){
    EStringBuilder sb = new EStringBuilder();

    sb.appendLine("FH - fly heading; TL - turn left; TR - turn right");
    sb.appendLine("MA - maintain altitude; CM - climb; DM - descend");
    sb.appendLine("SE - speed exact; SM - speed at most; SL - speed at least; SR - own speed");
    sb.appendLine("T - then; AA - after altitude; AN - after fix; AS - after speed");
    sb.appendLine("CT - contact tower; CA - contact approach; CC - contact center");
    sb.appendLine("C - cleared for approach (see details); C (I|II|III|G|V|R) [runway]");
    sb.appendLine("PD - proceed direct");
    sb.appendLine("SH - shortcut to");
    sb.appendLine("H - hold (see details); H P [navaid] - hold as published");

    return sb.toString();
  }

  @Override
  public String getHelp(String commandPrefix) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

}

