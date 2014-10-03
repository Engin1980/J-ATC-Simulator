/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.airplanes;

import jatcsimlib.global.KeyItems;

/**
 *
 * @author Marek
 */
public class Airplanes {

  public static Airplane tryGetByCallsingOrNumber(Iterable<Airplane> planes, String callsignOrNumber) {
    Airplane ret = null;
    char f = callsignOrNumber.charAt(0);
    if (f >= '0' && f <= '9') {
      // only partial callsign
      for (Airplane p : planes) {
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
      ret = Airplanes.tryGetByCallsign(planes, callsignOrNumber);
    }
    return ret;
  }

  public static Airplane tryGetByCallsign(Iterable<Airplane> planes, String callsign) {
    Callsign cs;
    try {
      cs = new Callsign(callsign);
    } catch (Exception ex) {
      return null;
    }
    return tryGetByCallsign(planes, cs);
  }
  
  public static Airplane tryGetByCallsign(Iterable<Airplane> planes, Callsign callsign){
    return KeyItems.tryGet(planes, callsign);
  }

  public static Airplane getByCallsing(Iterable<Airplane> planes, String callsign) {
    Callsign cs = new Callsign(callsign);
    return getByCallsing(planes, cs);
  }

  public static Airplane getByCallsing(Iterable<Airplane> planes, Callsign callsign) {
    return KeyItems.get(planes, callsign);
  }

  public static Airplane tryGetBySqwk(Iterable<Airplane> planes, String sqwk) {
    Squawk s;
    try {
      s = Squawk.create(sqwk);
    } catch (Exception ex) {
      return null;
    }
    return Airplanes.tryGetBySqwk(planes, s);
  }

  public static Airplane tryGetBySqwk(Iterable<Airplane> planes, Squawk sqwk) {
    for (Airplane p : planes) {
      if (p.getSqwk().equals(sqwk)) {
        return p;
      }
    }
    return null;
  }
}
