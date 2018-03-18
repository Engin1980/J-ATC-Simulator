package eng.jAtcSim.lib.speaking.parsing.shortParsing;

import eng.eSystem.EStringBuilder;
import eng.jAtcSim.lib.speaking.IFromAtc;

public abstract class SpeechParser<T extends IFromAtc> {

  public abstract String[] getPrefixes();

  public abstract String getPattern();

  public String getHelp() {
    EStringBuilder sb = new EStringBuilder();
    sb.appendLine(this.getClass().getSimpleName());
    sb.appendLine("\t" + this.getPattern());
    return sb.toString();
  }

  public abstract T parse(RegexGrouper line);
}
