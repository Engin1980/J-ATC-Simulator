/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.newLib.speeches.atc2atc;

import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.messaging.IMessageContent;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.speeches.ICommand;
import eng.jAtcSim.newLib.speeches.IRejectable;
import eng.jAtcSim.newLib.speeches.ISpeech;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

/**
 * @author Marek
 */
public class PlaneSwitchMessage implements ICommand, IRejectable, IMessageContent {

  public enum eMessageType {
    request,
    confirmation,
    cancelation,
    rejection
  }

  public final Callsign callsign;
  public final eMessageType messageType;
  public final String additionalMessageText;

  public PlaneSwitchMessage(Callsign callsign, eMessageType messageType) {
    this(callsign, messageType, null);
  }

  public PlaneSwitchMessage(Callsign callsign, eMessageType messageType, String additionalMessageText) {
    EAssert.Argument.isNotNull(callsign, "callsign");
    this.messageType = messageType;
    this.additionalMessageText = additionalMessageText;
    this.callsign = callsign;
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

  public String getMessageText() {
    StringBuilder sb = new StringBuilder();
    switch (this.messageType) {
      case confirmation:
        sb.append("accepted");
        break;
      case rejection:
        sb.append("rejected");
        break;
      case cancelation:
        sb.append("canceled");
        break;
      case request:
        sb.append("to you");
        break;
      default:
        throw new EEnumValueUnsupportedException(this.messageType);
    }
    if (additionalMessageText != null)
      sb.append(" ").append(additionalMessageText);
    return sb.toString();
  }

  public eMessageType getMessageType() {
    return messageType;
  }

  @Override
  public boolean isRejection() {
    return messageType == eMessageType.rejection;
  }

  @Override
  public String toString() {
    return sf("Plane-Switch-Message (%s)", this.messageType);
  }
}
