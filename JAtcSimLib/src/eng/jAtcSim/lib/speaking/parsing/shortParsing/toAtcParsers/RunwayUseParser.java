package eng.jAtcSim.lib.speaking.parsing.shortParsing.toAtcParsers;

import eng.jAtcSim.lib.speaking.fromAtc.atc2atc.RunwayUse;
import eng.jAtcSim.lib.speaking.parsing.shortParsing.RegexGrouper;
import eng.jAtcSim.lib.speaking.parsing.shortParsing.SpeechParser;

public class RunwayUseParser extends SpeechParser<RunwayUse> {
  private static final String[] prefixes = new String[]{"RWYUSE"};
  private static final String pattern = "RWYUSE";

  @Override
  public String[] getPrefixes() {
    return prefixes;
  }

  @Override
  public String getPattern() {
    return pattern;
  }

  @Override
  public RunwayUse parse(RegexGrouper line) {
    RunwayUse ret = new RunwayUse();
    return ret;
  }
}
