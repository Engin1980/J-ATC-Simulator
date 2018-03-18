package eng.jAtcSim.lib.speaking.parsing.shortParsing.fromAtcParsers;

import eng.jAtcSim.lib.speaking.fromAtc.atc2atc.RunwayCheck;
import eng.jAtcSim.lib.speaking.parsing.shortParsing.RegexGrouper;
import eng.jAtcSim.lib.speaking.parsing.shortParsing.SpeechParser;

public class RunwayCheckParser extends SpeechParser<RunwayCheck> {
  private static final String[] prefixes = new String[]{"rwycheck"};
  private static final String pattern = "RWYCHECK( ((TIME)|(DO)))( (.+))";

  @Override
  public String[] getPrefixes() {
    return prefixes;
  }

  @Override
  public String getPattern() {
    return pattern;
  }

  @Override
  public RunwayCheck parse(RegexGrouper line) {
    return null;
  }
}
