/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsimlib.commands;

import jatcsimlib.atcs.Atc;

/**
 *
 * @author Marek
 */
public class ContactCommand extends Command {
  private final Atc.eType atcType;

  public ContactCommand(Atc.eType atcType) {
    this.atcType = atcType;
  }

  public Atc.eType getAtcType() {
    return atcType;
  }
}
