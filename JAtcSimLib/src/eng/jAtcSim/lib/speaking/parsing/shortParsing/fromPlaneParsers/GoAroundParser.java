package eng.jAtcSim.lib.speaking.parsing.shortParsing.fromPlaneParsers;

import eng.jAtcSim.lib.speaking.fromAtc.commands.GoAroundCommand;
import eng.jAtcSim.lib.speaking.parsing.shortParsing.RegexGrouper;
import eng.jAtcSim.lib.speaking.parsing.shortParsing.SpeechParser;

public class GoAroundParser extends SpeechParser<GoAroundCommand> {

  private static final String[] prefixes = new String[]{"GA"};
  private static final String pattern = "(GA)";


  @Override
  public String[] getPrefixes() {
    return prefixes;
  }

  @Override
  public String getPattern() {
    return pattern;
  }

  @Override
  public GoAroundCommand parse(RegexGrouper line) {
    return new GoAroundCommand();
  }
}
