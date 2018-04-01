/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.atcs;

import eng.eSystem.collections.ReadOnlyList;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.AirplaneList;
import eng.jAtcSim.lib.exceptions.ENotSupportedException;
import eng.jAtcSim.lib.exceptions.ERuntimeException;
import eng.jAtcSim.lib.messaging.Message;
import eng.jAtcSim.lib.messaging.Messenger;
import eng.jAtcSim.lib.messaging.StringMessageContent;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Marek
 */
public class PlaneResponsibilityManager {

  public enum eState {

    ctr,
    ctr2app,
    ctr2appReady,
    app2ctr,
    app2ctrReady,
    app,
    app2twr,
    app2twrReady,
    twr2app,
    twr2appReady,
    twr
  }
  private static final PlaneResponsibilityManager me = new PlaneResponsibilityManager();
  private final Map<Airplane, eState> map = new HashMap<>();
  private final Map<Atc, AirplaneList> lst = new HashMap<>();
  private final AirplaneList all = new AirplaneList();
  private final List<Airplane.Airplane4Display> infos = new LinkedList<>();

  public static PlaneResponsibilityManager getInstance() {
    return me;
  }

  private PlaneResponsibilityManager() {
    lst.put(Acc.atcApp(), new AirplaneList());
    lst.put(Acc.atcCtr(), new AirplaneList());
    lst.put(Acc.atcTwr(), new AirplaneList());
  }

  public ReadOnlyList<Airplane.Airplane4Display> getPlanesToDisplay() {
    ReadOnlyList<Airplane.Airplane4Display> ret
        = new ReadOnlyList<>(this.infos);
    return ret;
  }

  public void registerPlane(Atc atc, Airplane plane) {
    if (map.containsKey(plane)) {
      throw new ERuntimeException("Second registration of already registered plane!");
    }

    map.put(plane, typeToState(atc));
    lst.get(atc).add(plane);
    all.add(plane);
    infos.add(plane.getPlane4Display());
    atc.registerNewPlaneUnderControl(plane, true);
  }

  public void unregisterPlane(Airplane plane) {
    if (!map.containsKey(plane)) {
      throw new ERuntimeException("Plane is not registered, cannot be unregistered!");
    }

    Atc atc = getResponsibleAtc(plane);

    map.remove(plane);
    lst.get(atc).remove(plane);
    all.remove(plane);
    infos.remove(plane.getPlane4Display());
    atc.unregisterPlaneUnderControl(plane, true);
  }

  public void requestSwitch(Atc from, Atc to, Airplane plane) {
    eState st = typeToState(from);
    if (map.get(plane) != st) {
      StringBuilder sb = new StringBuilder();
      sb.append("Code ").append(plane.getSqwk().toString()).append(" err: ");
      sb.append(" Cannot request switch. Atc ").append(from.getName()).append(" is not responsible for plane.");
      Message m = new Message(Messenger.SYSTEM, Acc.atcApp(), new StringMessageContent(sb.toString()));
      Acc.messenger().send(m);
      return;
    }

    switch (from.getType()) {
      case ctr:
        if (to.getType() != Atc.eType.app) {
          throw new ERuntimeException("Invalid request direction.");
        }
        if (map.get(plane) != eState.ctr) {
          throw new ERuntimeException("Not in ctr state, cannot switch.");
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

  public void confirmSwitch(Atc atc, Airplane plane) {
    if (isToSwitchToAtc(atc, plane)) {
      switch (map.get(plane)) {
        case app2ctr:
          map.put(plane, eState.app2ctrReady);
          break;
        case app2twr:
          map.put(plane, eState.app2twrReady);
          break;
        case ctr2app:
          map.put(plane, eState.ctr2appReady);
          break;
        case twr2app:
          map.put(plane, eState.twr2appReady);
          break;
        default:
          throw new ENotSupportedException();
      }
    } else {
      throw new ERuntimeException("Cannot switch plane, not ready to switch.");
    }
  }

  public void approveSwitch(Airplane plane) {
    if (isApprovedToSwitch(plane) == false) {
      throw new ERuntimeException("Plane not approved to switch!");
    }

    Atc oldAtc = getResponsibleAtc(plane);

    Atc newAtc = getApprovedAtc(plane);
    eState newState = typeToState(newAtc);
    map.put(plane, newState);

    oldAtc.unregisterPlaneUnderControl(plane, false);
    lst.get(oldAtc).remove(plane);
    lst.get(newAtc).add(plane);
    newAtc.registerNewPlaneUnderControl(plane, false);

    // this is the place, THE place!
  }

  public boolean isApprovedToSwitch(Airplane plane) {
    eState s = map.get(plane);
    return s == eState.app2ctrReady
        || s == eState.app2twrReady
        || s == eState.ctr2appReady
        || s == eState.twr2appReady;
  }

  public Atc getResponsibleAtc(Airplane plane) {
    eState s = map.get(plane);
    switch (s) {
      case app:
      case app2ctr:
      case app2twr:
      case app2ctrReady:
      case app2twrReady:
        return Acc.atcApp();
      case ctr:
      case ctr2app:
      case ctr2appReady:
        return Acc.atcCtr();
      case twr:
      case twr2app:
      case twr2appReady:
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

  public ReadOnlyList<Airplane> getAll() {
    return new ReadOnlyList<>(all);
  }

  boolean isToSwitch(Airplane p) {
    eState s = map.get(p);
    return s == eState.app2ctr
        || s == eState.app2twr
        || s == eState.ctr2app
        || s == eState.twr2app;
  }

  boolean isAskedToSwitch(Airplane p) {
    eState s = map.get(p);
    boolean ret = s == eState.app
        || s == eState.ctr
        || s == eState.twr;
    ret = !ret;
    return ret;
  }

  private Atc getApprovedAtc(Airplane p) {
    switch (map.get(p)) {
      case app2ctrReady:
        return Acc.atcCtr();
      case app2twrReady:
        return Acc.atcTwr();
      case ctr2appReady:
      case twr2appReady:
        return Acc.atcApp();
      default:
        throw new ENotSupportedException();
    }
  }

  protected AirplaneList getPlanes(Atc atc) {
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

  private boolean isIn(eState value, eState... set) {
    for (eState e : set) {
      if (e == value) {
        return true;
      }
    }
    return false;
  }
}
