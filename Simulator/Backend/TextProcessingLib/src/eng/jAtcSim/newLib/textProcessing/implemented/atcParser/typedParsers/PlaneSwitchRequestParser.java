package eng.jAtcSim.newLib.textProcessing.implemented.atcParser.typedParsers;

import eng.eSystem.collections.IList;
import eng.jAtcSim.newLib.shared.Squawk;
import eng.jAtcSim.newLib.speeches.atc.user2atc.PlaneSwitchRequest;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParser;

public class PlaneSwitchRequestParser extends TextSpeechParser<PlaneSwitchRequest> {

  private static final String[][] patterns = {
      {"\\d{4}", "\\d{2}[LRC]?/[A-Z0-9]+"},
      {"\\d{4}", "/[A-Z0-9]+"},
      {"\\d{4}", "\\d{2}[LRC]?"},
      {"\\d{4}"},
  };

  @Override
  public String getHelp() {
    String ret = super.buildHelpString(
        "Plane switch (hang-off) request (plane is selected by squawk code)",
        "{sqwk} - planes squawk code, keeps plane's routing\n" +
            "{sqwk} {rwy} - planes squawk code, changes planes expected runway\n" +
            "{sqwk} {rwy}/{route} - planes squawk code, changes plane expected runway and arrival/departure route\n" +
            "{sqwk} {rwy}/V - planes squawk code, changes plane expected runway, changes route to ATC vectoring\n" +
            "{sqwk} /{route} - planes squawk code, keeps plane expected runway, changes arrival/departure route\n" +
            "{sqwk} /V - planes squawk code, keeps plane expected runway, changes route to ATC vectoring\n",
        "Asks ATC to take plane into his/her control",
        "-1234\n-1234 24\n-1234 24/LOMKI4S\n-1234 /V");
    return ret;
  }

  @Override
  public String[][] getPatterns() {
    return patterns;
  }

  @Override
  public PlaneSwitchRequest parse(IList<String> blocks) {
    PlaneSwitchRequest ret;
    String sqwk = blocks.get(0);
    String runway = null;
    String route = null;
    if (blocks.count() == 2) {
      String[] tmp = blocks.get(1).split("/");
      if (tmp.length == 1) {
        runway = tmp[0];
        route = null;
      } else {
        runway = tmp[0].length() > 0 ? tmp[0] : null;
        route = tmp[1];
      }
    }
    ret = PlaneSwitchRequest.createFromUser(Squawk.create(sqwk), runway, route);
    return ret;
  }
}
