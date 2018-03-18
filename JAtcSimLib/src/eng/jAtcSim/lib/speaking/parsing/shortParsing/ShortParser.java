package eng.jAtcSim.lib.speaking.parsing.shortParsing;

import eng.eSystem.EStringBuilder;
import eng.eSystem.utilites.StringUtil;
import eng.jAtcSim.lib.exceptions.EInvalidCommandException;
import eng.jAtcSim.lib.speaking.IFromAtc;
import eng.jAtcSim.lib.speaking.ISpeech;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAtc.IAtc2Atc;
import eng.jAtcSim.lib.speaking.parsing.Parser;
import eng.jAtcSim.lib.speaking.parsing.ShortcutList;
import eng.jAtcSim.lib.speaking.parsing.shortParsing.fromAtcParsers.RunwayCheckParser;
import eng.jAtcSim.lib.speaking.parsing.shortParsing.fromPlaneParsers.*;

import java.util.ArrayList;
import java.util.List;

public class ShortParser extends Parser {

  private static final List<SpeechParser> planeParsers;
  private static final List<SpeechParser> atcParsers;
  private ShortcutList shortcuts = new ShortcutList();

  static {
    planeParsers = new ArrayList<>();
    planeParsers.add(new ChangeHeadingParser());
    planeParsers.add(new ChangeAltitudeParser());
    planeParsers.add(new ChangeSpeedParser());

    planeParsers.add(new AfterAltitudeParser());
    planeParsers.add(new AfterSpeedParser());
    planeParsers.add(new AfterNavaidParser());

    planeParsers.add(new ProceedDirectParser());
    planeParsers.add(new ShortcutParser());
    planeParsers.add(new HoldParser());

    planeParsers.add(new ClearedToApproachParser());

    planeParsers.add(new ContactParser());

    planeParsers.add(new ThenParser());
    planeParsers.add(new RadarContactConfirmationParser());

    planeParsers.add(new GoAroundParser());

    planeParsers.add(new ReportDivertTimeParser());
    planeParsers.add(new DivertParser());

    planeParsers.add(new SetAltitudeRestrictionParser());

    atcParsers = new ArrayList<>();
    atcParsers.add(new RunwayCheckParser());
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
    for (SpeechParser tmp : planeParsers) {
      for (String pref : tmp.getPrefixes()) {
        if (line.startsWith(pref + " ")) {
          return tmp;
        }
      }
    }

    return null;
  }

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
        // try shortcuts
        String trs = tryExpandByShortcut(tmp);
        if (trs != null) {
          tmp = trs;
          p = getSpeechParser(tmp);
        }

        if (p == null)
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

  @Override
  public ShortcutList getShortcuts() {
    return shortcuts;
  }

  @Override
  public String getHelp() {
    EStringBuilder sb = new EStringBuilder();

    sb.appendLine("FH - fly heading; TL - turn left; TR - turn right");
    sb.appendLine("MA - maintain altitude; CM - climb; DM - descend");
    sb.appendLine("SE - speed exact; SM - speed at most; SL - speed at least; SR - own speed");
    sb.appendLine("T - then; AA - antecedent altitude; AN - antecedent fix; AS - antecedent speed");
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

  @Override
  public IAtc2Atc parseAtc(String text) {
    text = normalizeCommandsInString(text);
    SpeechParser p = getSpeechParser(text);

    if (p == null)
      throw new EInvalidCommandException("Failed to parse atc message prefix.",
          text.substring(0, text.length() - text.length()),
          text);

    RegexGrouper rg = RegexGrouper.apply(text, p.getPattern());

    if (rg == null) {
      throw new EInvalidCommandException("Failed to parse command. Probably invalid syntax?",
          text.substring(0, text.length() - text.length()),
          text);
    }

    IAtc2Atc ret = (IAtc2Atc) p.parse(rg);
    return ret;
  }

  private String tryExpandByShortcut(String txt) {
    String ret;
    String firstWord = StringUtil.getUntil(txt, " ");
    String rest;
    if (firstWord.length() < txt.length())
      rest = txt.substring(firstWord.length());
    else rest = "";
    String exp = this.getShortcuts().tryGet(firstWord);
    if (exp != null)
      ret = exp + rest;
    else
      ret = null;
    return ret;
  }

}

