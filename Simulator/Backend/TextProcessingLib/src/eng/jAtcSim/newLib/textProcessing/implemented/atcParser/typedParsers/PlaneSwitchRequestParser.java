package eng.jAtcSim.newLib.textProcessing.implemented.atcParser.typedParsers;

import eng.eSystem.collections.IList;
import eng.jAtcSim.newLib.shared.Squawk;
import eng.jAtcSim.newLib.speeches.atc.planeSwitching.PlaneSwitchRequest;
import eng.jAtcSim.newLib.speeches.atc.planeSwitching.PlaneSwitchRequestRouting;
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

    if (runway != null || route != null){
      ret = new PlaneSwitchRequest(Squawk.create(sqwk), new PlaneSwitchRequestRouting(runway, route));
    }
     else
       ret = new PlaneSwitchRequest(Squawk.create(sqwk), false);
    return ret;
  }
}

/*

//TODO exported from UserAtc class
// if not required anymore, can be deleted

private Tuple<SwitchRoutingRequest, String> decodeAdditionalRouting(String text, Callsign callsign) {
    //TODO rewrite using some smart message, to not use parsing here
    EAssert.Argument.isNotNull(callsign, "callsign");
    IAirplane plane = Context.Internal.getPlane(callsign);

    Matcher m = Pattern.compile("(\\d{1,2}[lrcLRC]?)?(\\/(.+))?").matcher(text);
    EAssert.isTrue(m.find(), sf("Unable to decode switch routing request from '%s'.", text));

    ActiveRunwayThreshold threshold;
    DARoute route;

    if (m.group(1) == null)
      threshold = plane.getRouting().getAssignedRunwayThreshold();
    else {
      threshold = Context.getArea().getAirport().tryGetRunwayThreshold(m.group(1));
      if (threshold == null) {
        return new Tuple<>(null, "Unable to find runway threshold {" + m.group(1) + "}.");
      }
    }

    if (m.group(3) == null) {
      if (threshold == plane.getRouting().getAssignedRunwayThreshold())
        route = null;
      else {
        throw new ToDoException("Implement this");
//        route = plane.isArrival()
//            ? threshold.getArrivalRouteForPlane(plane.getType(), plane.getSha().getTargetAltitude(), plane.getRouting().getEntryExitPoint(), true)
//            : threshold.getDepartureRouteForPlane(plane.getType(), plane.getRouting().getEntryExitPoint(), true);
      }
    } else if (m.group(3).toUpperCase().equals("V")) {
      route = DARoute.createNewVectoringByFix(plane.getRouting().getEntryExitPoint());
    } else {
      route = threshold.getRoutes().tryGetFirst(q -> q.getName().equals(m.group(3)));
      if (route == null)
        return new Tuple<>(null, "Unable to find route {" + m.group((3) + "} for runway threshold {" + threshold.getName() + "}."));
    }

    Tuple<SwitchRoutingRequest, String> ret;
//    Tuple<SwitchRoutingRequest, String> ret = new Tuple<>(new SwitchRoutingRequest(threshold, route), null);
    if (threshold == plane.getRouting().getAssignedRunwayThreshold() && route.getName().equals(plane.getRouting().getAssignedDARouteName()))
      ret = new Tuple<>(null, null);
    else
      ret = new Tuple<>(new SwitchRoutingRequest(threshold, route), null);
    return ret;
  }
 */
