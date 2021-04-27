package eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers;

import eng.eSystem.EStringBuilder;
import eng.eSystem.Tuple;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.collections.IReadOnlySet;
import eng.eSystem.exceptions.ApplicationException;
import eng.eSystem.utilites.ReflectionUtils;
import eng.eSystem.utilites.RegexUtils;
import eng.jAtcSim.newLib.speeches.base.ISpeech;

import java.lang.reflect.Type;
import java.util.Arrays;

public abstract class TextSpeechParser<T extends ISpeech> {

  private final IReadOnlySet<String> prefixes = this.getPatterns()
          .select(q -> new Tuple<>(q, q.indexOf(' ')))
          .select(q -> q.getB() < 0 ? q.getA() : q.getA().substring(0, q.getB()))
          .toSet();

  public abstract IReadOnlyList<String> getPatterns();

  public abstract String getHelp();

  public abstract T parse(int patternIndex, RegexUtils.RegexGroups groups);

  public String getCommandName() {
    Type[] types = ReflectionUtils.getParameterizedTypes(this);
    String ret = types[0].getTypeName();
    return ret;
  }

  public final IReadOnlySet<String> getPrefixes() {
    return prefixes;
  }

  protected int getInt(IList<String> lst, int index) {
    String s = lst.get(index);
    int ret;
    try {
      ret = Integer.parseInt(s);
    } catch (Exception ex) {
      throw new ApplicationException("Unable to parseOld " + s + " to integer.");
    }
    return ret;
  }

  protected String buildHelpString(String name, String[][] patterns, String description, String examples) {
    EStringBuilder esb = new EStringBuilder();
    for (String[] pattern : patterns) {
      esb.appendItems(Arrays.asList(pattern), "\n");
    }
    String ret = this.buildHelpString(name, esb.toString(), description, examples);
    return ret;
  }

  protected String buildHelpString(String name, String syntax, String description, String examples) {
    EStringBuilder esb = new EStringBuilder();
    esb.append("** ").append(name).appendLine(" **");
    esb.appendLine("* Description *");
    esb.appendLine(description.replace("\n", "\r\n"));
    esb.appendLine("* Syntax *");
    esb.appendLine(syntax.replace("\n", "\r\n"));
    esb.appendLine("* Examples *");
    esb.appendLine(examples.replace("\n", "\r\n"));
    return esb.toString();
  }
}
