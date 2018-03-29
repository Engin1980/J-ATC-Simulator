package eng.jAtcSim.lib.speaking.parsing.shortParsing.toPlaneParsers;

import eng.jAtcSim.lib.speaking.fromAtc.commands.DivertCommand;
import eng.jAtcSim.lib.speaking.parsing.shortParsing.RegexGrouper;
import eng.jAtcSim.lib.speaking.parsing.shortParsing.SpeechParser;

public class DivertParser extends SpeechParser<DivertCommand> {

  private static final String[] prefixes = new String[]{"DVT"};
  private static final String pattern = "(DVT)";

  @Override
  public String[] getPrefixes() {
    return prefixes;
  }

  @Override
  public String getPattern() {
    return pattern;
  }

  @Override
  public DivertCommand parse(RegexGrouper line) {
    return new DivertCommand();
  }
}
