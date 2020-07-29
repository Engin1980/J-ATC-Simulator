package eng.jAtcSim.newLib.textProcessing.implemented.planeParser;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.utilites.NumberUtils;
import eng.jAtcSim.newLib.speeches.SpeechList;
import eng.jAtcSim.newLib.speeches.airplane.IForPlaneSpeech;
import eng.jAtcSim.newLib.textProcessing.IWithHelp;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextParsing;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParserList;
import eng.jAtcSim.newLib.textProcessing.parsing.EInvalidCommandException;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParser;
import eng.jAtcSim.newLib.textProcessing.implemented.planeParser.typedParsers.*;
import eng.jAtcSim.newLib.textProcessing.parsing.IPlaneParser;
import eng.jAtcSim.newLib.textProcessing.parsing.shortcuts.IWithShortcuts;
import eng.jAtcSim.newLib.textProcessing.parsing.shortcuts.ShortcutList;

public class PlaneParser implements IPlaneParser, IWithShortcuts<String>, IWithHelp {

  private static final TextSpeechParserList<IForPlaneSpeech> planeParsers;

  private final ShortcutList<String> shortcuts = new ShortcutList<>();

  static {
    planeParsers = new TextSpeechParserList<>();
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
    return planeParsers.getHelp();
  }

  @Override
  public String getHelp(Object cmd) {
    return planeParsers.getHelp((String) cmd);
  }

  @Override
  public ShortcutList<String> getShortcuts() {
    return shortcuts;
  }

  @Override
  public SpeechList<IForPlaneSpeech> parse(Object input) {
    String line = (String) input;
    IList<String> tokens = TextParsing.tokenize(line);
    SpeechList<IForPlaneSpeech> ret = new SpeechList<>();

    IList<String> toDo = new EList<>(tokens);
    IList<String> done = new EList<>();

    while (!toDo.isEmpty()) {
      TextSpeechParser<? extends IForPlaneSpeech> p = planeParsers.get(toDo);

      if (p == null) {
        // try shortcuts
        IList<String> trs = tryExpandByShortcut(toDo);
        if (trs != null)
          toDo = trs;
        p = planeParsers.get(toDo);

        if (p == null)
          throw new EInvalidCommandException("Failed to parseOld command prefix.",
              TextParsing.toLineString(done),
              TextParsing.toLineString(toDo));
      }

      IList<String> used;
      try {
        used = TextParsing.getInterestingBlocks(toDo, done, p);
      } catch (Exception ex) {
        throw new EInvalidCommandException("Failed to parseOld command via parser " + p.getClass().getName() + ". Probably invalid syntax?.",
            TextParsing.toLineString(done),
            TextParsing.toLineString(toDo));
      }
      if (used == null) {
        throw new EInvalidCommandException("Failed to parseOld command via parser " + p.getClass().getName() + ". Probably invalid syntax?.",
            TextParsing.toLineString(done),
            TextParsing.toLineString(toDo));
      }

      IForPlaneSpeech cmd = p.parse(used);
      ret.add(cmd);

    }

    return ret;
  }

  private IList<String> tryExpandByShortcut(IReadOnlyList<String> tokens) {
    IList<String> ret;
    String firstWord = tokens.get(0);
    String exp = this.getShortcuts().tryGet(firstWord);
    if (exp == null)
      ret = null;
    else {
      IList<String> tmp = TextParsing.tokenize(exp);
      ret = new EList<>();
      ret.addMany(tmp);
      ret.addMany(tokens);
      ret.removeAt(tmp.size());
    }

    return ret;
  }
}
