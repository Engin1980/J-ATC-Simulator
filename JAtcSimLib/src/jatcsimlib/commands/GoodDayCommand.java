/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.commands;

import jatcsimlib.airplanes.Callsign;

/**
 *
 * @author Marek Vajgl
 */
public class GoodDayCommand extends Command {
  private final Callsign callsign;
  private final String altitudeInfoText;

  public GoodDayCommand(Callsign callsign, String altitudeInfoText) {
    if (callsign == null)
      throw new IllegalArgumentException("Argument \"callsign\" cannot be null.");
    if (altitudeInfoText == null)
      throw new IllegalArgumentException("Argument \"altitudeInfoText\" cannot be null.");
    
    this.callsign = callsign;
    this.altitudeInfoText = altitudeInfoText;
  }

  public Callsign getCallsign() {
    return callsign;
  }

  public String getAltitudeInfoText() {
    return altitudeInfoText;
  }

  
}
