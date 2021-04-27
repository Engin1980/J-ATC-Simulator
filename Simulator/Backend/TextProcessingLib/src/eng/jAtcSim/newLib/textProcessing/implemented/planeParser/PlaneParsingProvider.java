package eng.jAtcSim.newLib.textProcessing.implemented.planeParser;

import eng.eSystem.Triple;
import eng.eSystem.collections.ISet;
import eng.eSystem.utilites.NumberUtils;
import eng.eSystem.utilites.RegexUtils;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.speeches.SpeechList;
import eng.jAtcSim.newLib.speeches.airplane.IForPlaneSpeech;
import eng.jAtcSim.newLib.textProcessing.IWithHelp;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParser;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParserList;
import eng.jAtcSim.newLib.textProcessing.implemented.planeParser.typedParsers.*;
import eng.jAtcSim.newLib.textProcessing.parsing.EInvalidCommandException;
import eng.jAtcSim.newLib.textProcessing.parsing.IPlaneParsingProvider;
import eng.jAtcSim.newLib.textProcessing.parsing.shortcuts.IWithShortcuts;
import eng.jAtcSim.newLib.textProcessing.parsing.shortcuts.ShortcutList;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaneParsingProvider implements IPlaneParsingProvider, IWithShortcuts<String>, IWithHelp {

  private static final TextSpeechParserList<IForPlaneSpeech> planeParsers;

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

  private final ShortcutList<String> shortcuts = new ShortcutList<>();

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
    return NumberUtils.isBetweenOrEqual('A', c, 'Z') || NumberUtils.isBetweenOrEqual('0', c, '9');
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
  public final SpeechList<IForPlaneSpeech> parse(Object input) {
    String line = (String) input.toString().toUpperCase();
    StringBuilder todo = new StringBuilder(line);
    StringBuilder done = new StringBuilder();
    SpeechList<IForPlaneSpeech> ret = new SpeechList<>();

    while (todo.length() > 0) {
      ISet<TextSpeechParser<? extends IForPlaneSpeech>> parsers = planeParsers.getAllByPrefixes(todo.toString());

      if (parsers.isEmpty()) {
        if (tryExpandByShortcut(todo)) continue;
        else
          throw new EInvalidCommandException("Failed to parse command prefix.",
                  done.toString(), todo.toString());
      } else if (parsers.size() > 1) {
        throw new EInvalidCommandException("There are multiple ways to parse command prefix (probably internal error?).",
                done.toString(), todo.toString());
      }

      IForPlaneSpeech tmp = this.parseWithParser(parsers.getFirst(), todo, done);
      ret.add(tmp);
    }

    return ret;
  }

  private IForPlaneSpeech parseWithParser(TextSpeechParser<? extends IForPlaneSpeech> parser, StringBuilder todo, StringBuilder done) {
    Triple<Integer, RegexUtils.RegexGroups, Integer> trgs = findFirstMatchingGroupSet(parser, todo.toString());

    todo = todo.delete(0, trgs.getC());
    trimStringBuilder(todo);

    if (done.length() > 0 && done.charAt(done.length() - 1) != ' ')
      done.append(" ");
    done.append(done);

    IForPlaneSpeech ret = parser.parse(trgs.getA(), trgs.getB());
    return ret;
  }

  protected void trimStringBuilder(StringBuilder sb) {
    while (sb.length() > 0 && sb.charAt(0) == ' ')
      sb.delete(0, 1);
    while (sb.length() > 0 && sb.charAt(sb.length() - 1) == ' ')
      sb.delete(sb.length() - 1, sb.length());
  }

  private Triple<Integer, RegexUtils.RegexGroups, Integer> findFirstMatchingGroupSet(TextSpeechParser<? extends IForPlaneSpeech> parser, String todo) {
    Triple<Integer, RegexUtils.RegexGroups, Integer> ret = null;
    for (int i = 0; i < parser.getPatterns().size(); i++) {
      Pattern p = Pattern.compile(parser.getPatterns().get(i));
      Matcher m = p.matcher(todo);
      if (m.find()) {
        ret = new Triple<>(i, new RegexUtils.RegexGroups(m, true), m.group(0).length());
        break;
      }
    }

    EAssert.isNotNull(ret);

    return ret;
  }

  private boolean tryExpandByShortcut(StringBuilder todo) {
    boolean hasSpace = todo.toString().contains(" ");
    String firstWord;
    if (hasSpace)
      firstWord = todo.substring(0, todo.toString().indexOf(" "));
    else firstWord = todo.toString();
    Optional<String> exp = this.getShortcuts().tryGet(firstWord);
    if (exp.isPresent())
      if (hasSpace)
        todo.replace(0, firstWord.length(), exp.get() + " ");
      else {
        todo.replace(0, todo.length(), exp.get());
      }
    return exp.isPresent();
  }
}
