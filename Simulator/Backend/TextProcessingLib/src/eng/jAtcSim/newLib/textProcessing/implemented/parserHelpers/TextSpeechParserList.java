package eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers;

import eng.eSystem.EStringBuilder;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.collections.ISet;
import eng.eSystem.utilites.RegexUtils;
import eng.jAtcSim.newLib.speeches.base.ISpeech;

import java.util.Arrays;
import java.util.regex.Pattern;

public class TextSpeechParserList<T extends ISpeech> {

  private final IList<TextSpeechParser<? extends T>> inner = new EList<>();

  public void add(TextSpeechParser<? extends T> parser) {
    inner.add(parser);
  }

  public ISet<TextSpeechParser<? extends T>> getAllByPatterns(String txt) {
    ISet<TextSpeechParser<? extends T>> ret = inner
            .where(q -> q.getPatterns()
                    .isAny(p -> RegexUtils.isMatch(txt, p))).toSet();
    return ret;
  }

  public ISet<TextSpeechParser<? extends T>> getAllByPrefixes(String txt) {
    String beginning = txt.contains(" ") ? txt.substring(0, txt.indexOf(' ')) : txt;
    ISet<TextSpeechParser<? extends T>> ret = inner.where(q -> q.getPrefixes().contains(beginning)).toSet();
    return ret;
  }

  public String getHelp() {
    EStringBuilder ret = new EStringBuilder();
    for (TextSpeechParser<? extends T> parser : this.getAll()) {
      String[] prefixes = parser.getPrefixes().toArray(String.class);
      ret.appendItems(Arrays.asList(prefixes), "/");
      ret.append(" - " + parser.getCommandName());
      ret.appendLine();
    }
    return ret.toString();
  }

  public String getHelp(String cmd) {
    TextSpeechParser<? extends T> p = this.getByPrefix(cmd);
    if (p == null)
      return null;
    else
      return p.getHelp();
  }

  private IReadOnlyList<TextSpeechParser<? extends T>> getAll() {
    return inner;
  }

  private TextSpeechParser<? extends T> getByPrefix(
          String prefix) {
    for (TextSpeechParser<? extends T> tmp : inner) {
      for (String s : tmp.getPrefixes()) {
        Pattern p = Pattern.compile("^(" + s + ")$");
        if (p.matcher(prefix).find()) {
          return tmp;
        }
      }
    }
    return null;
  }
}
