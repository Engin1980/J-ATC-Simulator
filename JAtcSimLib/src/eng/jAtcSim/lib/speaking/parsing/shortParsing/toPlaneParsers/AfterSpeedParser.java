package eng.jAtcSim.lib.speaking.parsing.shortParsing.toPlaneParsers;

import eng.jAtcSim.lib.speaking.fromAtc.commands.afters.AfterSpeedCommand;
import eng.jAtcSim.lib.speaking.parsing.shortParsing.RegexGrouper;
import eng.jAtcSim.lib.speaking.parsing.shortParsing.SpeechParser;

public class AfterSpeedParser extends SpeechParser<AfterSpeedCommand> {

  private static final String[] prefixes = new String[]{"AS"};
  private static final String pattern = "AS (\\d{1,3})";

  @Override
  public String[] getPrefixes() {
    return prefixes;
  }

  @Override
  public String getPattern() {
    return pattern;
  }

  @Override
  public AfterSpeedCommand parse(RegexGrouper rg) {
    int s = rg.getInt(1);
    AfterSpeedCommand ret = new AfterSpeedCommand(s);
    return ret;
  }
}
