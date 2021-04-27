package eng.jAtcSim.newLib.textProcessing.implemented.atcParser.typedParsers;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.utilites.RegexUtils;
import eng.jAtcSim.newLib.speeches.atc.user2atc.RunwayInUseRequest;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParser;

public class RunwayInUseRequestParser extends TextSpeechParser<RunwayInUseRequest> {

  private static final IReadOnlyList<String> patterns = EList.of(
          "RWYUSE( CHANGE)?");

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
  public IReadOnlyList<String> getPatterns() {
    return patterns;
  }

  @Override
  public RunwayInUseRequest parse(int patternIndex, RegexUtils.RegexGroups groups) {
    RunwayInUseRequest ret = new RunwayInUseRequest(
            groups.tryGetString(1)
                    .map(q -> RunwayInUseRequest.eType.changeNowRequest)
                    .orElse(RunwayInUseRequest.eType.informationRequest));
    return ret;
  }
}
