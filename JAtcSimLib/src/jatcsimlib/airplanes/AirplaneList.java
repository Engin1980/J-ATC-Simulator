/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.airplanes;

import jatcsimlib.exceptions.ERuntimeException;
import jatcsimlib.global.KeyList;
import java.util.Arrays;

/**
 *
 * @author Marek
 */
public class AirplaneList extends KeyList<Airplane, Callsign> {

  public Airplane tryGetByCallsingOrNumber(String callsignOrNumber) {
    Airplane ret = null;
    char f = callsignOrNumber.charAt(0);
    if (f >= '0' && f <= '9') {
      // only partial callsign
      for (Airplane p : this) {
        if (p.getCallsign().getNumber().equals(callsignOrNumber)) {
          if (ret == null) {
            ret = p;
          } else {
            ret = null;
            break;
          }
        }
      }
    } else {
      // full callsign
      ret = tryGetByCallsign(callsignOrNumber);
    }
    return ret;
  }

  public Airplane tryGetByCallsign(String callsign) {
    Callsign cs;
    try{
    cs = new Callsign(callsign);
    } catch (Exception ex){
      return null;
    }
    return this.tryGet(cs);
  }
  
  public Airplane getByCallsing(String callsign){
    Callsign cs = new Callsign(callsign);
    return this.get(cs);
  }

  public Airplane tryGetBySqwk(String sqwk) {
    Squawk s = new Squawk(sqwk);
    return this.tryGet(s);
  }

  public Airplane tryGet(Squawk sqwk) {
    for (Airplane p : this) {
      if (p.getSqwk().equals(sqwk)) {
        return p;
      }
    }
    return null;
  }
}
