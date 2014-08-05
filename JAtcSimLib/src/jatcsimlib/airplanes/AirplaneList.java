/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsimlib.airplanes;

import jatcsimlib.global.KeyList;

/**
 *
 * @author Marek
 */
public class AirplaneList extends KeyList<Airplane, Callsign> {
  
  public Airplane get(String callsign){
    Callsign cs = new Callsign(callsign);
    return this.get(cs);
  }
}
