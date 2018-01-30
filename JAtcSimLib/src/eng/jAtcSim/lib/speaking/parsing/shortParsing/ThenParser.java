package eng.jAtcSim.lib.speaking.parsing.shortParsing;

import eng.jAtcSim.lib.speaking.fromAtc.commands.ThenCommand;
import eng.jAtcSim.lib.speaking.ICommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ThenCommand;

class ThenParser extends SpeechParser<ThenCommand> {

  private static final String[] prefixes = new String[]{"T"};
  private static final String pattern = "T";

  @Override
  String[] getPrefixes() {
    return prefixes;
  }

  @Override
  String getPattern() {
    return pattern;
  }

  @Override
  ThenCommand parse(RegexGrouper rg) {
    ThenCommand ret = new ThenCommand();
    return ret;
  }
}
