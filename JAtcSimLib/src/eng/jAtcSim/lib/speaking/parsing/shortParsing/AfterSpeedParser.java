package eng.jAtcSim.lib.speaking.parsing.shortParsing;

import jatcsimlib.speaking.ICommand;
import jatcsimlib.speaking.fromAtc.commands.afters.AfterSpeedCommand;

class AfterSpeedParser extends SpeechParser<AfterSpeedCommand> {

  private static final String[] prefixes = new String[]{"AS"};
  private static final String pattern = "AS (\\d{1,3})";

  @Override
  String[] getPrefixes() {
    return prefixes;
  }

  @Override
  String getPattern() {
    return pattern;
  }

  @Override
  AfterSpeedCommand parse(RegexGrouper rg) {
    int s = rg.getInt(1);
    AfterSpeedCommand ret = new AfterSpeedCommand(s);
    return ret;
  }
}
