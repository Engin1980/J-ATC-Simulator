/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.lib.speaking.fromAtc.atc2atc;

import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.pilots.interfaces.forAirplane.IAirplaneRO;
import eng.jAtcSim.lib.airplanes.pilots.interfaces.forAirplane.IAirplaneWriteSimple;
import eng.jAtcSim.lib.speaking.fromAtc.IAtc2Atc;

/**
 * @author Marek
 */
public class PlaneSwitchMessage implements IAtc2Atc {

  public enum eMessageType {
    request,
    confirmation,
    cancelation,
    rejection
  }

  public final IAirplaneRO plane;
  public final eMessageType messageType;
  public final String additionalMessageText;

  public PlaneSwitchMessage(IAirplaneRO plane, eMessageType messageType) {
    this(plane, messageType, null);
  }

  public PlaneSwitchMessage(IAirplaneRO plane, eMessageType messageType, String additionalMessageText) {
    this.plane = plane;
    this.messageType = messageType;
    this.additionalMessageText = additionalMessageText;
  }

  public String getAsString() {
    if (plane.getPilot().getRoutingModule().getAssignedRoute() == null)
      throw new EApplicationException("Plane " + plane.getFlight().getCallsign() + " does not have assigned route.");
    if (plane.getPilot().getRoutingModule().getAssignedRunwayThreshold() == null)
      throw new EApplicationException("Plane " + plane.getFlight().getCallsign() + " does not have assigned threshold.");

    String ret =
        String.format("%1$s (%2$s) [%3$s] via %4$s/%5$s",
            plane.getSqwk().toString(),
            plane.getFlight().getCallsign().toString(),
            this.getMessageText(),
            plane.getPilot().getRoutingModule().getAssignedRoute().getName(),
            plane.getPilot().getRoutingModule().getAssignedRunwayThreshold().getName()
        );

    return ret;
  }

  public eMessageType getMessageType() {
    return messageType;
  }

  @Override
  public String toString() {
    return "Msg{" + plane + " -/ " + this.getMessageText() + '}';
  }


  @Override
  public boolean isRejection() {
    return messageType == eMessageType.rejection;
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
}
