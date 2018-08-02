/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.atcs;

import eng.eSystem.collections.*;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.xmlSerialization.XmlIgnore;
import eng.eSystem.xmlSerialization.XmlSerializationException;
import eng.eSystem.xmlSerialization.XmlSerializer;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.AirplaneList;
import eng.jAtcSim.lib.global.logging.CommonRecorder;
import eng.jAtcSim.lib.messaging.Message;
import eng.jAtcSim.lib.messaging.Messenger;
import eng.jAtcSim.lib.messaging.StringMessageContent;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

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

  private final IMap<Airplane, eState> map = new EMap<>();
  private final IMap<Atc, AirplaneList> lst = new EMap<>();
  private final AirplaneList all = new AirplaneList(true);
  @XmlIgnore
  private final List<Airplane.Airplane4Display> infos = new LinkedList<>();

  public PlaneResponsibilityManager() {
  }

  public void init(){
    if (lst.isEmpty()) { // non-empty after load
      lst.set(Acc.atcApp(), new AirplaneList(true));
      lst.set(Acc.atcCtr(), new AirplaneList(true));
      lst.set(Acc.atcTwr(), new AirplaneList(true));
    }

    for (Airplane plane : all) {
      infos.add(plane.getPlane4Display());
    }
  }

  public ReadOnlyList<Airplane.Airplane4Display> getPlanesToDisplay() {
    ReadOnlyList<Airplane.Airplane4Display> ret
        = new ReadOnlyList<>(this.infos);
    return ret;
  }

  public void registerPlane(Atc atc, Airplane plane) {
    if (map.containsKey(plane)) {
      throw new EApplicationException(sf("Second registration of already registered plane %s!", plane.getCallsign()));
    }

    map.set(plane, typeToState(atc));
    lst.get(atc).add(plane);
    all.add(plane);
    infos.add(plane.getPlane4Display());
    atc.registerNewPlaneUnderControl(plane, true);
  }

  public void unregisterPlane(Airplane plane) {
    if (!map.containsKey(plane)) {
      throw new EApplicationException(sf("Plane %s is not registered, cannot be unregistered!", plane.getCallsign()));
    }

    Atc atc = getResponsibleAtc(plane);

    map.remove(plane);
    lst.get(atc).remove(plane);
    all.remove(plane);
    infos.remove(plane.getPlane4Display());

    Acc.atcApp().removePlaneDeletedFromGame(plane);
    Acc.atcTwr().removePlaneDeletedFromGame(plane);
    Acc.atcCtr().removePlaneDeletedFromGame(plane);
  }

  public void requestSwitch(Atc from, Atc to, Airplane plane) {
    eState st = typeToState(from);
    if (map.get(plane) != st) {
      return;
    }

    switch (from.getType()) {
      case ctr:
        if (to.getType() != Atc.eType.app) {
          throw new EApplicationException("Invalid request direction.");
        }
        if (map.get(plane) != eState.ctr) {
          throw new EApplicationException("Not in ctr state, cannot switch.");
        }

        map.set(plane, eState.ctr2app);
        break;
      case app:

        if (map.get(plane) != eState.app) {
          throw new EApplicationException("Not int APP state, cannot switch.");
        }

        if (to.getType() == Atc.eType.ctr) {
          map.set(plane, eState.app2ctr);
        } else if (to.getType() == Atc.eType.twr) {
          map.set(plane, eState.app2twr);
        } else {
          throw new UnsupportedOperationException();
        }

        break;
      case twr:
        if (to.getType() != Atc.eType.app) {
          throw new EApplicationException("Invalid request direction.");
        }
        if (map.get(plane) != eState.twr) {
          throw new EApplicationException("Not in TWR state, cannot switch.");
        }

        map.set(plane, eState.twr2app);
        break;
      default:
        throw new EEnumValueUnsupportedException(from.getType());
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
        throw new EEnumValueUnsupportedException(targetAtc.getType());
    }
  }

  public void confirmSwitch(Atc atc, Airplane plane) {
    if (isToSwitchToAtc(atc, plane)) {
      switch (map.get(plane)) {
        case app2ctr:
          map.set(plane, eState.app2ctrReady);
          break;
        case app2twr:
          map.set(plane, eState.app2twrReady);
          break;
        case ctr2app:
          map.set(plane, eState.ctr2appReady);
          break;
        case twr2app:
          map.set(plane, eState.twr2appReady);
          break;
        default:
          throw new EEnumValueUnsupportedException(map.get(plane));
      }
    } else {
      throw new EApplicationException(sf("Cannot switch plane %s, not ready to switch.", plane.getCallsign()));
    }
  }

  public void approveSwitch(Airplane plane) {
    if (isApprovedToSwitch(plane) == false) {
      throw new IllegalArgumentException(sf("Plane %s not approved to switch!", plane.getCallsign()));
    }

    Atc oldAtc = getResponsibleAtc(plane);

    Atc newAtc = getApprovedAtc(plane);
    eState newState = typeToState(newAtc);
    map.set(plane, newState);

    oldAtc.unregisterPlaneUnderControl(plane);
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

  public boolean isRequestedToSwitch(Airplane plane, Atc atc){
    eState s = map.get(plane);
    boolean ret;
    ret = (s == eState.app2ctr && atc.getType()==Atc.eType.ctr)
        || (s == eState.app2twr && atc.getType()==Atc.eType.twr);
    return ret;
  }

  public eState getState(Airplane plane){
    return map.get(plane);
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
        throw new EEnumValueUnsupportedException(s);
    }
  }

  public void refuseSwitch(Atc atc, Airplane plane) {
    if (isToSwitchToAtc(atc, plane)) {
      eState s = map.get(plane);
      switch (s) {
        case app2ctr:
        case app2twr:
          map.set(plane, eState.app);
          break;
        case ctr2app:
          map.set(plane, eState.ctr);
          break;
        case twr2app:
          map.set(plane, eState.twr);
          break;
        default:
          throw new EEnumValueUnsupportedException(s);
      }
    } else {

    }
  }

  public boolean isRegistered(Airplane plane) {
    return map.containsKey(plane);
  }

  public IReadOnlyList<Airplane> getAll() {
    return new EList<>(all);
  }

  public void save(XElement elm) {
/*
  private final IMap<Airplane, eState> map = new EMap<>();
  private final IMap<Atc, AirplaneList> lst = new EMap<>();
  private final AirplaneList all = new AirplaneList();
 */

    XmlSerializer ser = new XmlSerializer();

    XElement tmp;

    try {

      tmp = new XElement("planes");
      for (Airplane airplane : all) {
        XElement tmpAp = new XElement("plane");
        airplane.save(tmpAp);
        tmp.addElement(tmpAp);
      }

      tmp = new XElement("states");
      IMap<String, String> remap = map.select(q -> q.getCallsign().toString(), q -> q.toString());
      ser.serialize(tmp, remap);
      elm.addElement(tmp);

      tmp = new XElement("atc");
      IMap<String, IList<String>> relst = lst.select(q -> q.getName(), q -> q.select(o -> o.getCallsign().toString()));
      ser.serialize(tmp, relst);
      elm.addElement(tmp);

      // infos not serialized, should be created during deserialization

    } catch (XmlSerializationException e) {
      throw new EApplicationException("Failed to store PlaneResponsibilityManager.", e);
    }
  }

  public void abortSwitch(Airplane plane,Atc oldAtc, Atc newAtc) {
    eState renewState = typeToState(newAtc);
    map.set(plane, renewState);

    oldAtc.unregisterPlaneUnderControl(plane);
    lst.get(oldAtc).remove(plane);
    lst.get(newAtc).add(plane);
    newAtc.registerNewPlaneUnderControl(plane, false);
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
        throw new EEnumValueUnsupportedException(map.get(p));
    }
  }

  protected AirplaneList getPlanes(Atc atc) {
    //TODO není to moc pomalé?
    AirplaneList nw = new AirplaneList(false);
    nw.add(lst.get(atc));
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
        throw new EEnumValueUnsupportedException(atc.getType());
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
