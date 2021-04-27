package eng.jAtcSim.newLib.textProcessing.implemented.atcParser;

import eng.eSystem.Triple;
import eng.eSystem.collections.ISet;
import eng.eSystem.utilites.RegexUtils;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.speeches.atc.IAtcSpeech;
import eng.jAtcSim.newLib.textProcessing.IWithHelp;
import eng.jAtcSim.newLib.textProcessing.implemented.atcParser.typedParsers.PlaneSwitchRequestParser;
import eng.jAtcSim.newLib.textProcessing.implemented.atcParser.typedParsers.RunwayInUseRequestParser;
import eng.jAtcSim.newLib.textProcessing.implemented.atcParser.typedParsers.RunwayMaintenanceRequestParser;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParser;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParserList;
import eng.jAtcSim.newLib.textProcessing.parsing.EInvalidCommandException;
import eng.jAtcSim.newLib.textProcessing.parsing.IAtcParsingProvider;
import eng.jAtcSim.newLib.textProcessing.parsing.shortcuts.IWithShortcuts;
import eng.jAtcSim.newLib.textProcessing.parsing.shortcuts.ShortcutList;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AtcParsingProvider implements IAtcParsingProvider, IWithShortcuts<String>, IWithHelp {

  private static final TextSpeechParserList<IAtcSpeech> atcParsers;

  static {
    atcParsers = new TextSpeechParserList<>();
    atcParsers.add(new RunwayMaintenanceRequestParser());
    atcParsers.add(new RunwayInUseRequestParser());
//    atcParsers.add(new PlaneSwitchRequestCancelationParser());
    atcParsers.add(new PlaneSwitchRequestParser());
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
    return (tmp.length() == 0 || tmp.charAt(0) == '+' || tmp.charAt(0) == '-');
  }

  @Override
  public String getHelp() {
    return atcParsers.getHelp();
  }

  @Override
  public String getHelp(Object cmd) {
    String tag = (String) cmd;
    return atcParsers.getHelp(tag);
  }

  @Override
  public ShortcutList<String> getShortcuts() {
    return shortcuts;
  }

  @Override
  public IAtcSpeech parse(Object input) {
    String line = (String) input;
    StringBuilder todo = new StringBuilder(line);
    StringBuilder done = new StringBuilder();

    ISet<TextSpeechParser<? extends IAtcSpeech>> parsers = atcParsers.get(todo.toString());
    if (parsers.size() == 0)
      throw new EInvalidCommandException("Failed to parse command prefix.",
              done.toString(), todo.toString());
    else if (parsers.size() > 1)
      throw new EInvalidCommandException("There are multiple ways to parse command prefix (probably internal error?).",
              done.toString(), todo.toString());

    IAtcSpeech ret = this.parseWithParser(parsers.getFirst(), todo, done);
    return ret;
  }

  private IAtcSpeech parseWithParser(TextSpeechParser<? extends IAtcSpeech> parser, StringBuilder todo, StringBuilder done) {
    Triple<Integer, RegexUtils.RegexGroups, Integer> trgs = findFirstMatchingGroupSet(parser, todo.toString());

    todo = todo.delete(0, trgs.getC());
    trimStringBuilder(todo);

    if (done.charAt(done.length() - 1) != ' ')
      done.append(" ");
    done.append(" ").append(done);

    IAtcSpeech ret = parser.parse(trgs.getA(), trgs.getB());
    return ret;
  }

  private Triple<Integer, RegexUtils.RegexGroups, Integer> findFirstMatchingGroupSet(TextSpeechParser<? extends IAtcSpeech> parser, String todo) {
    Triple<Integer, RegexUtils.RegexGroups, Integer> ret = null;
    for (int i = 0; i < parser.getPatterns().size(); i++) {
      Pattern p = Pattern.compile(parser.getPatterns().get(i));
      Matcher m = p.matcher(todo);
      if (m.find()) {
        ret = new Triple<>(i, new RegexUtils.RegexGroups(m), m.group(0).length());
        break;
      }
    }

    EAssert.isNotNull(ret);

    return ret;
  }

  protected void trimStringBuilder(StringBuilder sb) {
    while (sb.charAt(0) == ' ')
      sb.delete(0, 1);
    while (sb.charAt(sb.length() - 1) == ' ')
      sb.delete(sb.length() - 1, sb.length());
  }

}
