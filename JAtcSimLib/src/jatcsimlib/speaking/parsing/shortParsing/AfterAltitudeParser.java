package jatcsimlib.speaking.parsing.shortParsing;

import jatcsimlib.speaking.commands.Command;
import jatcsimlib.speaking.commands.afters.AfterAltitudeCommand;

class AfterAltitudeParser extends SpeechParser {

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
  Command parse(RegexGrouper rg) {
    int alt = rg.getInt(1) * 100;
    Command ret = new AfterAltitudeCommand(alt);
    return ret;
  }
}
