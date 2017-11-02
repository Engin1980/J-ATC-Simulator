/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsimlib.atcs;

import jatcsimlib.airplanes.Airplane;
import jatcsimlib.messaging.IContent;
import jatcsimlib.newMessaging.IMessageContent;

/**
 *
 * @author Marek
 */
public class PlaneSwitchMessage implements IContent, IMessageContent {

  public String getAsString() {
    
    String ret =
        String.format("%1$s (%2$s) [%3$s]",
            plane.getSqwk().toString(),
            plane.getCallsign().toString(),
            message);
    
    return ret;
  }
  
  public final Airplane plane;
  public final String message;

  public PlaneSwitchMessage(Airplane plane) {
    this(plane, "");
  }

  public PlaneSwitchMessage(Airplane plane, String message) {
    this.plane = plane;
    this.message = message;
  }

  @Override
  public String toString() {
    return "Msg{" + plane + " -/ " + message + '}';
  }
  
  
}
