package eng.jAtcSim.lib.speaking.parsing.shortParsing;

import eng.jAtcSim.lib.speaking.fromAtc.commands.DivertCommand;

public class DivertParser extends SpeechParser<DivertCommand> {

  private static final String[] prefixes = new String[]{"DVT"};
  private static final String pattern = "(DVT)";

  @Override
  String[] getPrefixes() {
    return prefixes;
  }

  @Override
  String getPattern() {
    return pattern;
  }

  @Override
  DivertCommand parse(RegexGrouper line) {
    return new DivertCommand();
  }
}
