package jatcsimlib.speaking.parsing.shortParsing;

import jatcsimlib.global.EStringBuilder;
import jatcsimlib.speaking.Speech;
import jatcsimlib.speaking.commands.Command;

public abstract class SpeechParser {

  abstract String[] getPrefixes();

  abstract String getPattern();

  public String getHelp() {
    EStringBuilder sb = new EStringBuilder();
    sb.appendLine(this.getClass().getSimpleName());
    sb.appendLine("\t" + this.getPattern());
    return sb.toString();
  }

  abstract Speech parse(RegexGrouper line);
}
