package eng.jAtcSim.lib.speaking.parsing.shortBlockParser.toAtcParsers;

import eng.eSystem.EStringBuilder;
import eng.eSystem.collections.IList;
import eng.jAtcSim.lib.speaking.fromAtc.atc2atc.RunwayUse;
import eng.jAtcSim.lib.speaking.parsing.shortBlockParser.SpeechParser;

import java.util.Arrays;

public class RunwayUseParser extends SpeechParser<RunwayUse> {
  private static final String[][] patterns = {{"RWYUSE"}};
  public String getHelp() {
    String ret = super.buildHelpString(
        "Runway in use",
        "-RWYUSE",
        "Asks Tower ATC about the runway in use and its expected change.",
        "-RWYUSE");
    return ret;
  }
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
