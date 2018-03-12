package eng.jAtcSim.lib.speaking.parsing.shortParsing;

import eng.jAtcSim.lib.speaking.fromAtc.commands.GoAroundCommand;

public class GoAroundParser extends SpeechParser<GoAroundCommand> {

  private static final String[] prefixes = new String[]{"GA"};
  private static final String pattern = "(GA)";


  @Override
  String[] getPrefixes() {
    return prefixes;
  }

  @Override
  String getPattern() {
    return pattern;
  }

  @Override
  GoAroundCommand parse(RegexGrouper line) {
    return new GoAroundCommand();
  }
}
