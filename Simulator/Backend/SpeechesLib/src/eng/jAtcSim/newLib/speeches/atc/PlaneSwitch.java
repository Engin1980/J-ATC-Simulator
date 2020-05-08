package eng.jAtcSim.newLib.speeches.atc;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.Callsign;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

/**
 * @author Marek
 */
public class PlaneSwitch implements IAtcSpeech {

  private final Callsign callsign;
  private final boolean repeated;

  public PlaneSwitch(Callsign callsign, boolean isRepeated) {
    EAssert.Argument.isNotNull(callsign, "callsign");
    this.callsign = callsign;
    this.repeated = isRepeated;
  }

  public PlaneSwitch(Callsign callsign) {
    this(callsign, false);
  }

  public boolean isRepeated() {
    return repeated;
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


  public Callsign getCallsign() {
    return callsign;
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
    return sf("Plane-Switch-Message (%s)", this.callsign.toString());
  }
}
