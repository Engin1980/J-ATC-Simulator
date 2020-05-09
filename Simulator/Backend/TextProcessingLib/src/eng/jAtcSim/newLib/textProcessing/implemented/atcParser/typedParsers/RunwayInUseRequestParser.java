package eng.jAtcSim.newLib.textProcessing.implemented.atcParser.typedParsers;

import eng.eSystem.collections.IList;
import eng.jAtcSim.newLib.speeches.atc.user2atc.RunwayInUseRequest;
import eng.jAtcSim.newLib.textProcessing.parsing.textParsing.SpeechParser;

public class RunwayInUseRequestParser extends SpeechParser<RunwayInUseRequest> {
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
  public RunwayInUseRequest parse(IList<String> blocks) {
    RunwayInUseRequest ret = new RunwayInUseRequest(
        blocks.size() == 2 ? RunwayInUseRequest.eType.changeNowRequest : RunwayInUseRequest.eType.informationRequest);
    return ret;
  }
}
