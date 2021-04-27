package eng.jAtcSim.newLib.textProcessing.implemented.atcParser.typedParsers;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.exceptions.UnexpectedValueException;
import eng.eSystem.utilites.RegexUtils;
import eng.jAtcSim.newLib.speeches.atc.user2atc.RunwayMaintenanceRequest;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParser;

public class RunwayMaintenanceRequestParser extends TextSpeechParser<RunwayMaintenanceRequest> {

  private static final IReadOnlyList<String> patterns = EList.of(
          "RWYCHECK TIME (\\d+[RLC]?)",
          "RWYCHECK TIME",
          "RWYCHECK DO (\\d+[RLC]?)",
          "RWYCHECK DO");

  public String getHelp() {
    String ret = super.buildHelpString(
            "Runway check",
            "-RWYCHECK TIME {rwy} - estimated check time of the specified runway\n" +
                    "-RWYCHECK TIME - estimated check time of all runways\n" +
                    "-RWYCHECK DO {rwy} - asks Tower ATC to do maintenance of specified runway now\n" +
                    "-RWYCHECK DO - asks Tower ATC to do maintenance of active runway\n",
            "Asks Tower ATC about the expected runway check and maintenance time and duration OR\n" +
                    "Asks Tower ATC about the expected end of the maintenance in progress of the runway OR\n" +
                    "Asks Tower ATC to start maintenance of the specified runway now.",
            "-RWY CHECK TIME 26L\n" +
                    "-RWYCHECK TIME\n" +
                    "-RWYCHECK DO 26L\n" +
                    "-RWYCHECK DO");
    return ret;
  }

  @Override
  public IReadOnlyList<String> getPatterns() {
    return patterns;
  }

  @Override
  public RunwayMaintenanceRequest parse(int patternIndex, RegexUtils.RegexGroups groups) {
    String rwy = null;
    RunwayMaintenanceRequest.eType type;
    switch (patternIndex) {
      case 1:
        rwy = groups.getString(1);
      case 0:
        type = RunwayMaintenanceRequest.eType.askForTime;
        break;
      case 3:
        rwy = groups.getString(1);
      case 2:
        type = RunwayMaintenanceRequest.eType.doCheck;
        break;
      default:
        throw new UnexpectedValueException(patternIndex);
    }

    RunwayMaintenanceRequest ret = new RunwayMaintenanceRequest(rwy, type);
    return ret;
  }
}
