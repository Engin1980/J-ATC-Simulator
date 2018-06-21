package eng.jAtcSim.lib.speaking.parsing.shortBlockParser.toAtcParsers;

import eng.eSystem.collections.IList;
import eng.jAtcSim.lib.speaking.fromAtc.atc2atc.RunwayUse;
import eng.jAtcSim.lib.speaking.parsing.shortBlockParser.SpeechParser;

public class RunwayUseParser extends SpeechParser<RunwayUse> {
  private static final String[][] patterns = {{"RWYUSE"}};

  @Override
  public String[][] getPatterns() {
    return patterns;
  }

  @Override
  public RunwayUse parse(IList<String> blocks) {
    RunwayUse ret = new RunwayUse();
    return ret;
  }
}
