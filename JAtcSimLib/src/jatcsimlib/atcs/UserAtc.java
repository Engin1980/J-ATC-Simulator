/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsimlib.atcs;

import jatcsimlib.airplanes.Airplane;

/**
 *
 * @author Marek
 */
public class UserAtc extends Atc {

  public UserAtc(String airportIcao) {
    super(eType.app, airportIcao);
  }

  @Override
  public boolean isHuman() {
    return true;
  }

  @Override
  protected void _registerNewPlane(Airplane plane) {
    throw new UnsupportedOperationException("Not supported yet."); 
  }

}
