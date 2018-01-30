package eng.jAtcSim.lib.speaking.parsing.shortParsing;

import eng.jAtcSim.lib.speaking.ICommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.afters.AfterAltitudeCommand;

class AfterAltitudeParser extends SpeechParser<AfterAltitudeCommand> {

  private static final String[] prefixes = new String[]{"AA"};
  private static final String pattern = "AA (\\d{1,3})";

  @Override
  String[] getPrefixes() {
    return prefixes;
  }

  @Override
  String getPattern() {
    return pattern;
  }

  @Override
  AfterAltitudeCommand parse(RegexGrouper rg) {
    int alt = rg.getInt(1) * 100;
    AfterAltitudeCommand ret = new AfterAltitudeCommand(alt);
    return ret;
  }
}
