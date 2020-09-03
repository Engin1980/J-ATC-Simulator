package eng.jAtcSim.newLib.speeches.atc.user2atc;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.Squawk;
import eng.jAtcSim.newLib.speeches.atc.IAtcSpeech;
import eng.jAtcSim.newLib.speeches.atc.atc2user.RunwayInUseNotification;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

/**
 * @author Marek
 */
public class PlaneSwitchRequest implements IAtcSpeech {

  public static class Routing{
    public final String runwayThresholdName;
    public final String routeName;

    public Routing(String runwayThresholdName, String routeName) {
      this.runwayThresholdName = runwayThresholdName;
      this.routeName = routeName;
    }
  }

  public enum eType{
    inherit,
    request,
    requestRepeated,
    rerouting,
    cancelaton
  }

  public static PlaneSwitchRequest createInherit(Squawk squawk){
    PlaneSwitchRequest ret = new PlaneSwitchRequest(squawk, eType.inherit, null);
    return ret;
  }

  public static PlaneSwitchRequest createRequest(Squawk squawk, boolean repeated){
    PlaneSwitchRequest ret = new PlaneSwitchRequest(squawk,
        repeated ? eType.requestRepeated : eType.request,
        null);
    return ret;
  }

  public static PlaneSwitchRequest createRerouting(Squawk squawk, String runwayThresholdName, String routeName){
    PlaneSwitchRequest ret = new PlaneSwitchRequest(squawk, eType.rerouting, new Routing(runwayThresholdName, routeName));
    return ret;
  }

  public static PlaneSwitchRequest createCancelation(Squawk squawk){
    PlaneSwitchRequest ret = new PlaneSwitchRequest(squawk, eType.cancelaton, null);
    return ret;
  }

  private final eType type;
  private final Squawk squawk;
  private final Routing routing;

  private PlaneSwitchRequest(Squawk squawk, eType type, Routing routing) {
    EAssert.Argument.isNotNull(squawk, "squawk");
    EAssert.Argument.isTrue(routing == null || type == eType.rerouting, "Routing can be used only with re-routing type.");
    this.squawk = squawk;
    this.type = type;
    this.routing = routing;
  }

  public Squawk getSquawk() {
    return squawk;
  }

  public eType getType() {
    return type;
  }

  public Routing getRouting() {
    return routing;
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
