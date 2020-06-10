package eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers;

import eng.eSystem.EStringBuilder;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.jAtcSim.newLib.speeches.base.ISpeech;

import java.util.Arrays;
import java.util.regex.Pattern;

public class TextSpeechParserList<T extends ISpeech> {

  private final IList<TextSpeechParser<? extends T>> inner = new EList<>();

  public void add(TextSpeechParser<? extends T> parser) {
    inner.add(parser);
  }

  public IReadOnlyList<TextSpeechParser<? extends T>> getAll() {
    return inner;
  }

  public TextSpeechParser<? extends T> get(IReadOnlyList<String> tokens) {
    TextSpeechParser<? extends T> ret = getByPrefix(tokens.get(0));
    return ret;
  }

  public TextSpeechParser<? extends T> getByPrefix(
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

  public String getHelp(){
    EStringBuilder ret = new EStringBuilder();
    for (TextSpeechParser<? extends T> parser : this.getAll()) {
      String[] prefixes = parser.getPrefixes();
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
}