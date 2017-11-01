package jatcsimlib.speaking.parsing.shortParsing;

import jatcsimlib.speaking.commands.Command;
import jatcsimlib.speaking.commands.specific.ThenCommand;
import jatcsimlib.speaking.parsing.CmdParser;

class ThenParser extends SpeechParser {

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
  Command parse(RegexGrouper rg) {
    Command ret = new ThenCommand();
    return ret;
  }
}
