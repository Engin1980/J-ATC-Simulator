package eng.jAtcSim.newLib.speeches.atc.user2atc;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.Squawk;
import eng.jAtcSim.newLib.speeches.atc.IAtcSpeech;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

/**
 * @author Marek
 */
public class PlaneSwitchRequest implements IAtcSpeech {

  public static PlaneSwitchRequest createFromComputer(Squawk squawk, boolean isRepeated) {
    return new PlaneSwitchRequest(squawk, isRepeated, null, null);
  }

  public static PlaneSwitchRequest createFromComputer(Squawk squawk) {
    return createFromComputer(squawk, false);
  }

  public static PlaneSwitchRequest createFromUser(Squawk squawk, String runway, String route) {
    return new PlaneSwitchRequest(squawk, false, runway, route);
  }

  private final Squawk squawk;
  private final boolean repeated;
  private final String runwayName;
  private final String routeName;

  public PlaneSwitchRequest(Squawk squawk, boolean repeated, String runwayName, String routeName) {
    EAssert.Argument.isNotNull(squawk, "callsign");
    this.squawk = squawk;
    this.repeated = repeated;
    this.runwayName = runwayName;
    this.routeName = routeName;
  }

  public Squawk getSquawk() {
    return squawk;
  }

  public String getRouteName() {
    return routeName;
  }

  public String getRunwayName() {
    return runwayName;
  }

  //  public String getAsString() {
//    if (plane.getRoutingModule().getAssignedRoute() == null)
//      throw new EApplicationException("Plane " + plane.getFlightModule().getCallsign() + " does not have assigned route.");
//    if (plane.getRoutingModule().getAssignedRunwayThreshold() == null)
//      throw new EApplicationException("Plane " + plane.getFlightModule().getCallsign() + " does not have assigned threshold.");
//
//    String ret =
//        String.format("%1$s (%2$s) [%3$s] via %4$s/%5$s",
//            plane.getSqwk().toString(),
//            plane.getFlightModule().getCallsign().toString(),
//            this.getMessageText(),
//            plane.getRoutingModule().getAssignedRoute().getName(),
//            plane.getRoutingModule().getAssignedRunwayThreshold().getName()
//        );
//
//    return ret;
//  }

  public boolean isRepeated() {
    return repeated;
  }

//  public String getMessageText() {
//    StringBuilder sb = new StringBuilder();
//    switch (this.messageType) {
//      case confirmation:
//        sb.append("accepted");
//        break;
//      case rejection:
//        sb.append("rejected");
//        break;
//      case cancelation:
//        sb.append("canceled");
//        break;
//      case request:
//        sb.append("to you");
//        break;
//      default:
//        throw new EEnumValueUnsupportedException(this.messageType);
//    }
//    if (additionalMessageText != null)
//      sb.append(" ").append(additionalMessageText);
//    return sb.toString();
//  }

  @Override
  public String toString() {
    return sf("Plane-Switch-Message (%s)", this.squawk.toString());
  }
}
