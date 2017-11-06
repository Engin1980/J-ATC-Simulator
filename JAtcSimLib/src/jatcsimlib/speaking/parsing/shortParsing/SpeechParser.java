package jatcsimlib.speaking.parsing.shortParsing;

import jatcsimlib.global.EStringBuilder;
import jatcsimlib.speaking.IFromAtc;
import jatcsimlib.speaking.ISpeech;

public abstract class SpeechParser<T extends IFromAtc> {

  abstract String[] getPrefixes();

  abstract String getPattern();

  public String getHelp() {
    EStringBuilder sb = new EStringBuilder();
    sb.appendLine(this.getClass().getSimpleName());
    sb.appendLine("\t" + this.getPattern());
    return sb.toString();
  }

  abstract T parse(RegexGrouper line);
}
