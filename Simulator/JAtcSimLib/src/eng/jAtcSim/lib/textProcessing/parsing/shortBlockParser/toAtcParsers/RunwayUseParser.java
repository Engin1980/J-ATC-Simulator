package eng.jAtcSim.lib.textProcessing.parsing.shortBlockParser.toAtcParsers;

import eng.eSystem.collections.IList;
import eng.jAtcSim.lib.speaking.fromAtc.atc2atc.RunwayUse;
import eng.jAtcSim.lib.textProcessing.parsing.shortBlockParser.SpeechParser;

public class RunwayUseParser extends SpeechParser<RunwayUse> {
  private static final String[][] patterns = {
      {"RWYUSE", "CHANGE"},
      {"RWYUSE"}};

  public String getHelp() {
    String ret = super.buildHelpString(
        "Runway in use",
        "-RWYUSE - asks for information about runway in use and scheduled changes\n" +
            "-RWYUSE CHANGE - aks Tower ATC to change runway to scheduled as soon as possible\n",
        "Asks Tower ATC about the runway in use and its expected change, or asks for scheduled runway change.",
        "-RWYUSE\n-RWYUSE CHANGE");
    return ret;
  }

  @Override
  public String[][] getPatterns() {
    return patterns;
  }

  @Override
  public RunwayUse parse(IList<String> blocks) {
    boolean asksForChange = blocks.size() == 2;
    RunwayUse ret = new RunwayUse(asksForChange);
    return ret;
  }
}
