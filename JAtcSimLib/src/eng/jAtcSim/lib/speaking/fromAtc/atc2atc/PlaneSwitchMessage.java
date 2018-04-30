/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.lib.speaking.fromAtc.atc2atc;

import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.speaking.fromAtc.IAtc2Atc;

/**
 * @author Marek
 */
public class PlaneSwitchMessage implements IAtc2Atc {

  public final Airplane plane;
  public final String message;
  public final boolean rejection;

  public PlaneSwitchMessage(Airplane plane) {
    this(plane, false, "");
  }

  public PlaneSwitchMessage(Airplane plane, boolean rejection, String message) {
    this.plane = plane;
    this.message = message;
    this.rejection = rejection;
  }

  public String getAsString() {

    String ret =
        String.format("%1$s (%2$s) [%3$s]",
            plane.getSqwk().toString(),
            plane.getCallsign().toString(),
            message);

    return ret;
  }

  @Override
  public String toString() {
    return "Msg{" + plane + " -/ " + message + '}';
  }


  @Override
  public boolean isRejection() {
    return rejection;
  }
}
