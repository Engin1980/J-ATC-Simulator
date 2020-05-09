package eng.jAtcSim.newLib.textProcessing.implemented.planeParser;

import eng.eSystem.EStringBuilder;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.utilites.NumberUtils;
import eng.jAtcSim.newLib.speeches.SpeechList;
import eng.jAtcSim.newLib.speeches.airplane.IForPlaneSpeech;
import eng.jAtcSim.newLib.speeches.airplane.IPlaneSpeech;
import eng.jAtcSim.newLib.textProcessing.IWithHelp;
import eng.jAtcSim.newLib.textProcessing.old.base.EInvalidCommandException;
import eng.jAtcSim.newLib.textProcessing.parsing.textParsing.SpeechParser;
import eng.jAtcSim.newLib.textProcessing.implemented.planeParser.typedParsers.*;
import eng.jAtcSim.newLib.textProcessing.parsing.IPlaneParser;
import eng.jAtcSim.newLib.textProcessing.parsing.shortcuts.IWithShortcuts;
import eng.jAtcSim.newLib.textProcessing.parsing.shortcuts.ShortcutList;

import java.util.Arrays;
import java.util.regex.Pattern;

public class PlaneParser implements IPlaneParser, IWithShortcuts<String>, IWithHelp {

  private static final IList<SpeechParser<? extends IForPlaneSpeech>> planeParsers;

  private static SpeechParser<? extends IForPlaneSpeech> getSpeechParser(IReadOnlyList<String> tokens) {
    SpeechParser<? extends IForPlaneSpeech> ret = getParserByPrefix(tokens.get(0));
    return ret;
  }

  private static SpeechParser<? extends IForPlaneSpeech> getParserByPrefix(
      String prefix) {
    for (SpeechParser<? extends IForPlaneSpeech> tmp : PlaneParser.planeParsers) {
      for (String s : tmp.getPrefixes()) {
        Pattern p = Pattern.compile("^(" + s + ")$");
        if (p.matcher(prefix).find()) {
          return tmp;
        }
      }
    }
    return null;
  }
  private final ShortcutList<String> shortcuts = new ShortcutList<>();

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

    planeParsers.add(new AltitudeRestrictionCommandParser());
  }

  @Override
  public boolean acceptsType(Class<?> type) {
    return String.class.equals(type);
  }

  @Override
  public boolean canAccept(Object input) {
    if (input == null) return true;
    String tmp = (String) input;
    tmp = tmp.trim();
    if (tmp.length() == 0) return true;
    char c = tmp.charAt(0);
    return NumberUtils.isBetweenOrEqual('A',c,'Z') || NumberUtils.isBetweenOrEqual('0', c, '9');
  }

  @Override
  public String getHelp() {
    EStringBuilder ret = new EStringBuilder();
    ret.appendLine("* Plane commands *");
    ret.appendLine("Plane commands must be prefixed by a plane callsign.");
    for (SpeechParser<? extends IPlaneSpeech> parser : planeParsers) {
      String[] prefixes = parser.getPrefixes();
      ret.appendItems(Arrays.asList(prefixes), "/");
      ret.append(" - " + parser.getCommandName());
      ret.appendLine();
    }
    return ret.toString();
  }

  @Override
  public String getHelp(Object input) {
    String commandPrefix = (String) input;
    commandPrefix = commandPrefix.toUpperCase();
    SpeechParser<? extends IPlaneSpeech> p = getParserByPrefix(commandPrefix);
    if (p == null)
      return null;
    else
      return p.getHelp();
  }

  @Override
  public ShortcutList<String> getShortcuts() {
    return shortcuts;
  }

  @Override
  public SpeechList<IForPlaneSpeech> parse(Object input) {
    String line = (String) input;
    IList<String> tokens = tokenize(line);
    SpeechList<IForPlaneSpeech> ret = new SpeechList<>();

    IList<String> toDo = new EList<>(tokens);
    IList<String> done = new EList<>();

    while (!toDo.isEmpty()) {
      SpeechParser<? extends IForPlaneSpeech> p = getSpeechParser(toDo);

      if (p == null) {
        // try shortcuts
        IList<String> trs = tryExpandByShortcut(toDo);
        if (trs != null)
          toDo = trs;
        p = getSpeechParser(toDo);

        if (p == null)
          throw new EInvalidCommandException("Failed to parseOld command prefix.",
              toLineString(done),
              toLineString(toDo));
      }

      IList<String> used;
      try {
        used = getInterestingBlocks(toDo, done, p);
      } catch (Exception ex) {
        throw new EInvalidCommandException("Failed to parseOld command via parser " + p.getClass().getName() + ". Probably invalid syntax?.",
            toLineString(done),
            toLineString(toDo));
      }
      if (used == null) {
        throw new EInvalidCommandException("Failed to parseOld command via parser " + p.getClass().getName() + ". Probably invalid syntax?.",
            toLineString(done),
            toLineString(toDo));
      }

      IForPlaneSpeech cmd = p.parse(used);
      ret.add(cmd);

    }

    return ret;
  }

  private IList<String> getInterestingBlocks(IList<String> toDo, IList<String> done, SpeechParser<? extends IForPlaneSpeech> p) {
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

  private int getMatchingPatternIndex(IList<String> toDo, SpeechParser<? extends IForPlaneSpeech> parser) {
    int ret = -1;

    for (int i = 0; i < parser.getPatterns().length; i++) {
      String[] pattern = parser.getPatterns()[i];

      if (pattern.length > toDo.size()) continue; // pattern longer than input

      ret = i;
      for (int j = 0; j < pattern.length; j++) {
        Pattern p = Pattern.compile("^(" + pattern[j] + ")$");
        String pt = toDo.get(j);
        if (p.matcher(pt).find() == false) { // some part of pattern does not fit
          ret = -1;
          break;
        }
      }

      if (ret != -1) break;
    }

    return ret;
  }

  private String toLineString(IReadOnlyList<String> processed) {
    EStringBuilder ret = new EStringBuilder();
    ret.appendItems(processed, " ");
    return ret.toString();
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
    if (exp == null)
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
