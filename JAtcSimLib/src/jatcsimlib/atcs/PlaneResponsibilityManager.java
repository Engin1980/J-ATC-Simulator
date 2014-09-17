/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.atcs;

import jatcsimlib.Acc;
import jatcsimlib.airplanes.Airplane;
import jatcsimlib.airplanes.AirplaneList;
import jatcsimlib.exceptions.ENotSupportedException;
import jatcsimlib.exceptions.ERuntimeException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Marek
 */
public class PlaneResponsibilityManager {

  private static final PlaneResponsibilityManager me = new PlaneResponsibilityManager();

  public static PlaneResponsibilityManager getInstance() {
    return me;
  }

  boolean isToSwitch(Airplane p) {
    eState s = map.get(p);
    return s == eState.app2ctr
        || s == eState.app2twr
        || s == eState.ctr2app
        || s == eState.twr2app;
  }

  public enum eState {

    ctr,
    ctr2app,
    app2ctr,
    app,
    app2twr,
    twr2app,
    twr
  }

  private final Map<Airplane, eState> map = new HashMap<>();
  private final Map<Atc, AirplaneList> lst = new HashMap<>();

  private PlaneResponsibilityManager() {
    lst.put(Acc.atcApp(), new AirplaneList());
    lst.put(Acc.atcCtr(), new AirplaneList());
    lst.put(Acc.atcTwr(), new AirplaneList());
  }
  
  protected AirplaneList getPlanes(Atc atc){
    //TODO není to moc pomalé?
    AirplaneList nw = new AirplaneList();
    nw.addAll(lst.get(atc));
    return nw;
  }

  private eState typeToState(Atc atc) {
    switch (atc.getType()) {
      case ctr:
        return eState.ctr;
      case app:
        return eState.app;
      case twr:
        return eState.twr;
      default:
        throw new ENotSupportedException();
    }
  }

  public void registerPlane(Atc atc, Airplane plane) {
    if (map.containsKey(plane)) {
      throw new ERuntimeException("Second registration of already registered plane!");
    }

    map.put(plane, typeToState(atc));
    lst.get(atc).add(plane);
  }

  public void unregisterPlane(Airplane plane) {
    if (!map.containsKey(plane)) {
      throw new ERuntimeException("Plane is not registered, cannot be unregistered!");
    }

    map.remove(plane);
    Atc atc = getResponsibleAtc(plane);
    lst.get(atc).remove(plane);
  }

  public void requestSwitch(Atc from, Atc to, Airplane plane) {
    eState st = typeToState(from);
    if (map.get(plane) != st) {
      throw new ERuntimeException("Cannot request switch. Not responsible atc for airplane. Atc: " + from.getName() + ", plane: " + plane.getCallsign() + ", state: " + map.get(plane));
    }

    switch (from.getType()) {
      case ctr:
        if (to.getType() != Atc.eType.app) {
          throw new ERuntimeException("Invalid request direction.");
        }
        if (map.get(plane) != eState.ctr) {
          throw new ERuntimeException("Not in CTR state, cannot switch.");
        }

        map.put(plane, eState.ctr2app);
        break;
      case app:

        if (map.get(plane) != eState.app) {
          throw new ERuntimeException("Not int APP state, cannot switch.");
        }

        if (to.getType() == Atc.eType.ctr) {
          map.put(plane, eState.app2ctr);
        } else if (to.getType() == Atc.eType.twr) {
          map.put(plane, eState.app2twr);
        } else {
          throw new ENotSupportedException();
        }

        break;
      case twr:
        if (to.getType() != Atc.eType.app) {
          throw new ERuntimeException("Invalid request direction.");
        }
        if (map.get(plane) != eState.twr) {
          throw new ERuntimeException("Not in TWR state, cannot switch.");
        }

        map.put(plane, eState.twr2app);
        break;
      default:
        throw new ENotSupportedException();
    }
  }

  public boolean isToSwitchToAtc(Atc targetAtc, Airplane plane) {
    eState s = map.get(plane);
    switch (targetAtc.getType()) {
      case app:
        return isIn(s, eState.ctr2app, eState.twr2app);
      case twr:
        return isIn(s, eState.app2twr);
      case ctr:
        return isIn(s, eState.app2ctr);
      default:
        throw new ENotSupportedException();
    }
  }

  private boolean isIn(eState value, eState... set) {
    for (eState e : set) {
      if (e == value) {
        return true;
      }
    }
    return false;
  }

  public void confirmSwitch(Atc atc, Airplane plane) {
    if (isToSwitchToAtc(atc, plane)) {
      
      Atc oldAtc = getResponsibleAtc(plane);
      
      eState newState = typeToState(atc);
      map.put(plane, newState);
      
      lst.get(oldAtc).remove(plane);
      lst.get(atc).add(plane);
    } else {
      throw new ERuntimeException("Cannot switch plane, not ready to switch.");
    }
  }

  public Atc getResponsibleAtc(Airplane plane) {
    eState s = map.get(plane);
    switch (s) {
      case app:
      case app2ctr:
      case app2twr:
        return Acc.atcApp();
      case ctr:
      case ctr2app:
        return Acc.atcCtr();
      case twr:
      case twr2app:
        return Acc.atcTwr();
      default:
        throw new ENotSupportedException();
    }
  }

  public void refuseSwitch(Atc atc, Airplane plane) {
    if (isToSwitchToAtc(atc, plane)) {
      eState s = map.get(plane);
      switch (s) {
        case app2ctr:
        case app2twr:
          map.put(plane, eState.app);
          break;
        case ctr2app:
          map.put(plane, eState.ctr);
          break;
        case twr2app:
          map.put(plane, eState.twr);
          break;
        default:
          throw new ENotSupportedException();
      }
    } else {

    }
  }

  public boolean isRegistered(Airplane plane) {
    return map.containsKey(plane);
  }
}