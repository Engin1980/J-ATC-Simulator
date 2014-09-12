/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsimlib.atcs;

import jatcsimlib.Acc;
import jatcsimlib.airplanes.Airplane;
import jatcsimlib.commands.Command;
import jatcsimlib.exceptions.ENotSupportedException;
import java.util.List;

/**
 *
 * @author Marek
 */
public class UserAtc extends Atc {

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

  @Override
  public boolean isControllingAirplane(Airplane plane) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  public void elapseSecond() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  public void sendCommands(Airplane plane, List<Command> commands){
    Acc.messenger().addMessage(this, plane, commands);
  }
  public void sendCommands(Atc.eType type, String commandText){
    Atc atc = Acc.atc(type);
    Acc.messenger().addMessage(this, atc, commandText);
  }
  public void sendError (String message){
    Acc.messenger().addMessage(null, this, message);
  }
  public void sendSystem (String message){
    throw new ENotSupportedException();
  }
  
}
