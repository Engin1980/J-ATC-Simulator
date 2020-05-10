package eng.jAtcSim.newLib.textProcessing.implemented.atcParser.typedParsers;

import eng.eSystem.collections.IList;
import eng.jAtcSim.newLib.speeches.atc.user2atc.RunwayMaintenanceRequest;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParser;

public class RunwayMaintenanceRequestParser extends TextSpeechParser<RunwayMaintenanceRequest> {
  private static final String[][] patterns = {
      {"RWYCHECK", "TIME", ".+"},
      {"RWYCHECK", "TIME"},
      {"RWYCHECK", "DO", ".+"},
      {"RWYCHECK", "DO"}};

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
  public String[][] getPatterns() {
    return patterns;
  }

  @Override
  public RunwayMaintenanceRequest parse(IList<String> blocks) {
    String action = blocks.get(1);
    String rwyName = null;
    if (blocks.size() == 3)
      rwyName = blocks.get(2);

//    ActiveRunway rwy;
//    if (rwyName == null)
//      rwy = null;
//    else {
//      ActiveRunwayThreshold rt = Acc.airport().tryGetRunwayThreshold(rwyName);
//      if (rt == null) {
//        throw new EApplicationException("Unable to find threshold name " + rwyName + ".");
//      } else {
//        rwy = rt.getParent();
//      }
//    }

    RunwayMaintenanceRequest.eType type;
    switch (action) {
      case "DO":
        type = RunwayMaintenanceRequest.eType.doCheck;
        break;
      case "TIME":
        type = RunwayMaintenanceRequest.eType.askForTime;
        break;
      default:
        throw new UnsupportedOperationException();
    }

    RunwayMaintenanceRequest ret = new RunwayMaintenanceRequest(rwyName, type);
    return ret;
  }
}
