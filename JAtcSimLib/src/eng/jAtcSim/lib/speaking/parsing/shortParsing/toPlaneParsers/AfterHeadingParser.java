package eng.jAtcSim.lib.speaking.parsing.shortParsing.toPlaneParsers;

import eng.jAtcSim.lib.speaking.fromAtc.commands.afters.AfterHeadingCommand;
import eng.jAtcSim.lib.speaking.parsing.shortParsing.RegexGrouper;
import eng.jAtcSim.lib.speaking.parsing.shortParsing.SpeechParser;

public class AfterHeadingParser extends SpeechParser<AfterHeadingCommand> {

  private static final String[] prefixes = new String[]{"AH"};
  private static final String pattern = "AH (\\d{1,3)";

  @Override
  public String[] getPrefixes() {
    return prefixes;
  }

  @Override
  public String getPattern() {
    return pattern;
  }

  @Override
  public AfterHeadingCommand parse(RegexGrouper rg) {
    int hdg = rg.getInt(1);
    AfterHeadingCommand ret = new AfterHeadingCommand(hdg);
    return ret;
  }

}
