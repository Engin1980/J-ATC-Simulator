package jatcsimlib.speaking.parsing.shortParsing;

import jatcsimlib.speaking.ICommand;
import jatcsimlib.speaking.fromAtc.commands.ThenCommand;

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
