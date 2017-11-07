/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsimlib.speaking.fromAtc.commands;

import jatcsimlib.atcs.Atc;
import jatcsimlib.speaking.ICommand;
import jatcsimlib.speaking.fromAtc.IAtcCommand;

/**
 *
 * @author Marek
 */
public class ContactCommand implements IAtcCommand {
  private final Atc.eType atcType;

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
