/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.atcs;

import jatcsimlib.Acc;
import jatcsimlib.airplanes.Airplane;
import jatcsimlib.airplanes.AirplaneList;
import jatcsimlib.commands.CommandList;
import jatcsimlib.exceptions.ENotSupportedException;
import jatcsimlib.exceptions.ERuntimeException;
import jatcsimlib.messaging.IContent;

/**
 *
 * @author Marek
 */
public class UserAtc extends Atc {

  private final AirplaneList departures = new AirplaneList();
  private final AirplaneList departuresForCtr = new AirplaneList();
  private final AirplaneList arrivals = new AirplaneList();
  private final AirplaneList arrivalsForTwr = new AirplaneList();

  public UserAtc(AtcTemplate template) {
    super(template);
  }

  @Override
  public boolean isHuman() {
    return true;
  }

  @Override
  protected void _registerNewPlane(Airplane plane) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void elapseSecond() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  public void sendCommands(Airplane plane, CommandList commands) {
    Acc.messenger().addMessage(this, plane, commands);
  }

  public void sendCommands(Atc.eType type, Airplane plane) {
    Atc atc = Acc.atc(type);
    
    if (getPrm().isToSwitch(plane)){
      // je to ... -> APP
      
      if (getPrm().getResponsibleAtc(plane).getType() != type){
        // nesedi smer potvrzeni A predava na APP, ale APP potvrzuje na B
        sendError("SQWK " + plane.getSqwk() + " not in your control. You cannot ask for switch.");
      } else {
        // potvrdime
        getPrm().confirmSwitch(this, plane);
      }
    } else {
      // je nova zadost APP -> ???
      getPrm().requestSwitch(this, atc, plane);
    }

    PlaneSwitchMessage msg = new PlaneSwitchMessage(plane);
    Acc.messenger().addMessage(this, atc, msg);
  }

  public void sendError(String message) {
    Acc.messenger().addMessage(null, this, message);
  }

  public void sendSystem(String message) {
    throw new ENotSupportedException();
  }

}
