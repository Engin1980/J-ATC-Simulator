package eng.jAtcSim.newLib.textProcessing.implemented;

import eng.eSystem.Triple;
import eng.eSystem.utilites.RegexUtils;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.speeches.base.ISpeech;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParsingProviderUtils {
  public static <T extends ISpeech> T parseWithParser(TextSpeechParser<T> parser, StringBuilder todo, StringBuilder done) {
    Triple<Integer, RegexUtils.RegexGroups, Integer> trgs = findFirstMatchingGroupSet(parser, todo.toString());

    todo = todo.delete(0, trgs.getC());
    trimStringBuilder(todo);

    if (done.length() > 0 && done.charAt(done.length() - 1) != ' ')
      done.append(" ");
    done.append(done);

    T ret = parser.parse(trgs.getA(), trgs.getB());
    return ret;
  }

  public static void prepareStringBuilders(Object input, StringBuilder todo, StringBuilder done) {
    String line = ((String) input).trim().toUpperCase();
    EAssert.Argument.isTrue(todo.length() == 0);
    todo.append(line);
    EAssert.Argument.isTrue(done.length() == 0);
  }

  private static void trimStringBuilder(StringBuilder sb) {
    while (sb.length() > 0 && sb.charAt(0) == ' ')
      sb.delete(0, 1);
    while (sb.length() > 0 && sb.charAt(sb.length() - 1) == ' ')
      sb.delete(sb.length() - 1, sb.length());
  }

  private static <T> Triple<Integer, RegexUtils.RegexGroups, Integer> findFirstMatchingGroupSet(TextSpeechParser<? extends T> parser, String todo) {
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
}
