package eng.jAtcSim.lib.speaking.parsing.shortBlockParser;

import eng.eSystem.EStringBuilder;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.utilites.ArrayUtils;
import eng.jAtcSim.lib.exceptions.EInvalidCommandException;
import eng.jAtcSim.lib.speaking.IFromAtc;
import eng.jAtcSim.lib.speaking.ISpeech;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAtc.IAtc2Atc;
import eng.jAtcSim.lib.speaking.parsing.Parser;
import eng.jAtcSim.lib.speaking.parsing.ShortcutList;
import eng.jAtcSim.lib.speaking.parsing.shortBlockParser.toAtcParsers.*;
import eng.jAtcSim.lib.speaking.parsing.shortBlockParser.toPlaneParsers.*;
import sun.font.FontRunIterator;

import java.util.regex.Pattern;

public class ShortBlockParser extends Parser {

  private static final IList<SpeechParser> planeParsers;
  private static final IList<SpeechParser> atcParsers;
  private ShortcutList shortcuts = new ShortcutList();

  static {
    planeParsers = new EList<>();
    planeParsers.add(new ChangeHeadingParser());
    planeParsers.add(new ChangeAltitudeParser());
    planeParsers.add(new ChangeSpeedParser());

    planeParsers.add(new AfterAltitudeParser());
    planeParsers.add(new AfterSpeedParser());
    planeParsers.add(new AfterNavaidParser());
    planeParsers.add(new AfterRadialParser());
    planeParsers.add(new AfterDistanceParser());
    planeParsers.add(new AfterHeadingParser());

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

    atcParsers = new EList<>();
    atcParsers.add(new RunwayCheckParser());
    atcParsers.add(new RunwayUseParser());
  }

  private static SpeechParser getSpeechParser(IReadOnlyList<String> tokens) {
    for (SpeechParser tmp : planeParsers) {
      if (ArrayUtils.contains(tmp.getPrefixes(), tokens.get(0)))
        return tmp;
    }

    return null;
  }

  private static SpeechParser getAtcSpeechParser(IReadOnlyList<String> tokens) {
    for (SpeechParser tmp : atcParsers) {
      if (ArrayUtils.contains(tmp.getPrefixes(), tokens.get(0)))
        return tmp;
    }

    return null;
  }

  @Override
  public SpeechList<IFromAtc> parseMulti(String line) {
    IList<String> tokens = tokenize(line);
    SpeechList ret = new SpeechList();

    IList<String> toProcess = new EList<>(tokens);
    IList<String> processed = new EList<>(tokens);

    while (!toProcess.isEmpty()) {
      SpeechParser p = getSpeechParser(processed);

      if (p == null) {
        // try shortcuts
        IList<String> trs = tryExpandByShortcut(processed);
        p = getSpeechParser(trs);

        if (p == null)
          throw new EInvalidCommandException("Failed to parse command prefix.",
              toLineString(processed),
              toLineString(toProcess));
      }

      IList<String> used = null;
      try {
        used = getInterestingBlocks(toProcess, processed, p);
      } catch (Exception ex) {
        throw new EInvalidCommandException("Failed to parse command via parser " + p.getClass().getName() + ". Probably invalid syntax?.",
            toLineString(processed),
            toLineString(toProcess));
      }

      ISpeech cmd = p.parse(used);
      ret.add(cmd);

    }

    return ret;
  }

  @Override
  public ShortcutList getShortcuts() {
    return shortcuts;
  }

  @Override
  public String getHelp() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public String getHelp(String commandPrefix) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public IAtc2Atc parseAtc(String text) {
    IList<String> toDo = tokenize(text);
    IList<String> done = new EList<>();

    SpeechParser p = getAtcSpeechParser(toDo);

    if (p == null)
      throw new EInvalidCommandException("Failed to parse atc message prefix.",
          toLineString(toDo),
          toLineString(done));

    IList<String> used = null;
    try {
      used = getInterestingBlocks(toDo, done, p);
    } catch (Exception ex) {
      throw new EInvalidCommandException("Failed to parse command via parser " + p.getClass().getName() + ". Probably invalid syntax?.",
          toLineString(toDo),
          toLineString(done));
    }

    IAtc2Atc ret = (IAtc2Atc) p.parse(used);
    return ret;
  }

  private String toLineString(IReadOnlyList<String> processed) {
    EStringBuilder ret = new EStringBuilder();
    ret.appendItems(processed, " ");
    return ret.toString();
  }

  private IList<String> getInterestingBlocks(IList<String> toDo, IList<String> done, SpeechParser p) {
    IList<String> ret;
    int index = getMatchingPatternIndex(toDo, p);
    if (index < 0) {
      ret = null;
    } else {
      //moving pattern from "toDo" to "done" and to "ret"
      ret = new EList<>();
      for (int i = 0; i < p.getPatterns()[index].length; i++) {
        String s = toDo.get(0);
        toDo.removeAt(0);
        done.add(s);
        ret.add(s);
      }
    }
    return ret;
  }

  private int getMatchingPatternIndex(IList<String> toDo, SpeechParser parser) {
    int ret = -1;

    for (int i = 0; i < parser.getPatterns().length; i++) {
      String[] pattern = parser.getPatterns()[i];

      if (pattern.length > toDo.size()) continue; // pattern longer than input

      ret = i;
      for (int j = 0; j < pattern.length; j++) {
        Pattern p = Pattern.compile(pattern[j]);
        if (p.matcher(toDo.get(i)).find() == false) { // some part of pattern does not fit
          ret = -1;
          break;
        }
      }

      if (ret == -1) break;
    }

    return ret;
  }

  private IList<String> tokenize(String line) {
    IList<String> ret;
    if (line == null || line.isEmpty()) {
      return new EList<>();
    }
    line = line.toUpperCase();
    String[] pts = line.split(" ");
    ret = new EList<>(pts);
    ret.remove(q -> q.length() == 0);
    return ret;
  }

  private IList<String> tryExpandByShortcut(IReadOnlyList<String> tokens) {
    IList<String> ret;
    String firstWord = tokens.get(0);
    String exp = this.getShortcuts().tryGet(firstWord);
    if (exp != null)
      ret = null;
    else {
      IList<String> tmp = tokenize(exp);
      ret = new EList<>();
      ret.add(tmp);
      ret.add(tokens);
      ret.removeAt(tmp.size());
    }

    return ret;
  }

}

