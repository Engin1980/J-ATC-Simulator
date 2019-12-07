/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.newLib.speaking.fromAtc.commands;

;
import eng.eSystem.xmlSerialization.annotations.XmlConstructor;
import eng.jAtcSim.newLib.atcs.Atc;
import eng.jAtcSim.newLib.speaking.fromAtc.IAtcCommand;

/**
 *
 * @author Marek
 */
public class ContactCommand implements IAtcCommand {
  private final Atc.eType atcType;

  @XmlConstructor
  private ContactCommand() {
    atcType = Atc.eType.gnd;
  }

  public ContactCommand(Atc.eType atcType) {
    this.atcType = atcType;
  }

  public Atc.eType getAtcType() {
    return atcType;
  }

  @Override
  public String toString(){
    String ret = "Contact " + atcType.toString() + " {command}";

    return ret;
  }
}
