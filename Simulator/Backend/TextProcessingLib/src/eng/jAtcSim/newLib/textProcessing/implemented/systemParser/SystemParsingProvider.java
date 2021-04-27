package eng.jAtcSim.newLib.textProcessing.implemented.systemParser;

import eng.eSystem.Triple;
import eng.eSystem.collections.ISet;
import eng.eSystem.utilites.RegexUtils;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.speeches.airplane.IForPlaneSpeech;
import eng.jAtcSim.newLib.speeches.system.ISystemSpeech;
import eng.jAtcSim.newLib.speeches.system.ISystemUserRequest;
import eng.jAtcSim.newLib.textProcessing.IWithHelp;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParser;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParserList;
import eng.jAtcSim.newLib.textProcessing.implemented.systemParser.typedParser.*;
import eng.jAtcSim.newLib.textProcessing.parsing.EInvalidCommandException;
import eng.jAtcSim.newLib.textProcessing.parsing.ISystemParsingProvider;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SystemParsingProvider implements ISystemParsingProvider, IWithHelp {

  private static final TextSpeechParserList<ISystemUserRequest> systemParsers;

  static {
    systemParsers = new TextSpeechParserList<>();
    systemParsers.add(new MetarRequestParser());
    systemParsers.add(new ShortcutRequestParser());
    systemParsers.add(new TickSpeedRequestParser());
    systemParsers.add(new DeletePlaneRequestParser());
    systemParsers.add(new GetHelpRequestParser());
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
    return tmp.length() == 0 || tmp.charAt(0) == '?';
  }

  @Override
  public String getHelp() {
    return systemParsers.getHelp();
  }

  @Override
  public String getHelp(Object cmd) {
    return systemParsers.getHelp((String) cmd);
  }

  @Override
  public ISystemSpeech parse(Object input) {
    String line = (String) input;
    StringBuilder todo = new StringBuilder(line);
    StringBuilder done = new StringBuilder();

    ISet<TextSpeechParser<? extends ISystemUserRequest>> parsers = systemParsers.get(todo.toString());
    if (parsers.size() == 0)
      throw new EInvalidCommandException("Failed to parse command prefix.",
              done.toString(), todo.toString());
    else if (parsers.size() > 1)
      throw new EInvalidCommandException("There are multiple ways to parse command prefix (probably internal error?).",
              done.toString(), todo.toString());

    ISystemSpeech ret = this.parseWithParser(parsers.getFirst(), todo, done);
    return ret;
  }

  private ISystemSpeech parseWithParser(TextSpeechParser<? extends ISystemUserRequest> parser, StringBuilder todo, StringBuilder done) {
    Triple<Integer, RegexUtils.RegexGroups, Integer> trgs = findFirstMatchingGroupSet(parser, todo.toString());

    todo = todo.delete(0, trgs.getC());
    trimStringBuilder(todo);

    if (done.charAt(done.length() - 1) != ' ')
      done.append(" ");
    done.append(" ").append(done);

    ISystemSpeech ret = parser.parse(trgs.getA(), trgs.getB());
    return ret;
  }

  private Triple<Integer, RegexUtils.RegexGroups, Integer> findFirstMatchingGroupSet(TextSpeechParser<? extends ISystemUserRequest> parser, String todo) {
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
