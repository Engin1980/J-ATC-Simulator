package eng.jAtcSim.lib.speaking.parsing.shortParsing.fromPlaneParsers;

import eng.jAtcSim.lib.speaking.fromAtc.commands.ThenCommand;
import eng.jAtcSim.lib.speaking.ICommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ThenCommand;
import eng.jAtcSim.lib.speaking.parsing.shortParsing.RegexGrouper;
import eng.jAtcSim.lib.speaking.parsing.shortParsing.SpeechParser;

public class ThenParser extends SpeechParser<ThenCommand> {

  private static final String[] prefixes = new String[]{"T"};
  private static final String pattern = "T";

  @Override
  public String[] getPrefixes() {
    return prefixes;
  }

  @Override
  public String getPattern() {
    return pattern;
  }

  @Override
  public ThenCommand parse(RegexGrouper rg) {
    ThenCommand ret = new ThenCommand();
    return ret;
  }
}
