/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsimlib.atcs;

/**
 *
 * @author Marek
 */
public abstract class ComputerAtc extends Atc {

  public ComputerAtc(AtcTemplate template) {
    super(template);
  }
  
  @Override
  public boolean isHuman(){return false;}
  
}
