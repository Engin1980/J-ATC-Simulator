package jatcsimlib.speaking.parsing.shortParsing;

import jatcsimlib.speaking.commands.Command;
import jatcsimlib.speaking.commands.afters.AfterSpeedCommand;

class AfterSpeedParser extends SpeechParser {

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
  Command parse(RegexGrouper rg) {
    int s = rg.getInt(1);
    Command ret = new AfterSpeedCommand(s);
    return ret;
  }
}
