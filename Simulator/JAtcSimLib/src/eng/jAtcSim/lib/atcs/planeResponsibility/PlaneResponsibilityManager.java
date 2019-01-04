/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.atcs.planeResponsibility;

import com.sun.istack.internal.Nullable;
import eng.eSystem.collections.*;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.validation.Validator;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.Callsign;
import eng.jAtcSim.lib.atcs.Atc;
import eng.jAtcSim.lib.atcs.ComputerAtc;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class PlaneResponsibilityManager {

  public class PlaneResponsibilityManagerForAtc {

    public IReadOnlyList<Airplane> getPlanes(Atc atc) {
      IReadOnlyList<AirplaneResponsibilityInfo> tmp = dao.getByAtc(atc);
      IReadOnlyList<Airplane> ret = tmp.select(q -> q.getPlane());
      return ret;
    }

    public boolean isUnderSwitchRequest(Airplane plane, @Nullable Atc sourceAtc, @Nullable Atc targetAtc) {
      boolean ret;
      AirplaneResponsibilityInfo ai = dao.get(plane);
      ret = ai.getSwitchRequest() != null;
      if (ret && sourceAtc != null)
        ret = ai.getAtc() == sourceAtc;
      if (ret && targetAtc != null)
        ret = ai.getSwitchRequest().getAtc() == targetAtc;
      return ret;
    }

    public Atc getResponsibleAtc(Airplane plane) {
      return PlaneResponsibilityManager.this.getResponsibleAtc(plane);
    }

    public IReadOnlyList<Airplane> getSwitchRequestsToRepeatByAtc(Atc sender) {
      IReadOnlyList<AirplaneResponsibilityInfo> tmp = dao.getByAtc(sender);
      tmp = tmp.where(q -> q.getSwitchRequest() != null
          && q.getAtc() == sender
          && q.getSwitchRequest().isConfirmed() == false
          && q.getSwitchRequest().getRepeatRequestTime().isBefore(Acc.now()));
      tmp.forEach(q -> q.getSwitchRequest().updateLastRequestTime());
      IReadOnlyList<Airplane> ret = tmp.select(q -> q.getPlane());
      return ret;
    }

    public void createSwitchRequest(Atc sender, Atc targetAtc, Airplane plane) {
      Validator.isNotNull(sender);
      Validator.isNotNull(targetAtc);
      Validator.isNotNull(plane);

      AirplaneResponsibilityInfo ai = dao.get(plane);

      // auto-cancel
      if (ai.getAtc() == targetAtc && ai.getPlane().getTunedAtc() == sender) {
        ai.setAtc(sender);
        ai.setSwitchRequest(null);
        return;
      }

      if (ai.getSwitchRequest() != null)
        throw new EApplicationException("Airplane " + plane.getCallsign() + " is already under request switch from "
            + ai.getAtc().getType().toString() + " to " + ai.getSwitchRequest().getAtc().getType().toString() + ".");
      if (ai.getAtc() != sender)
        throw new EApplicationException("Airplane " + plane.getCallsign()
            + " is requested to be switched from incorrect atc. Current is "
            + ai.getAtc().getType().toString() + ", requested from is " + sender.getType().toString() + ".");

      if ((sender.getType() == Atc.eType.ctr || sender.getType() == Atc.eType.twr) && targetAtc.getType() != Atc.eType.app)
        throw new EApplicationException("Invalid request direction.");

      SwitchRequest sr = new SwitchRequest(targetAtc);
      ai.setSwitchRequest(sr);
    }

    public IReadOnlyList<Airplane> getConfirmedSwitchesByAtc(Atc sender) {
      IReadOnlyList<AirplaneResponsibilityInfo> tmp = dao.getByAtc(sender);
      IList<Airplane> ret = tmp
          .where(q -> q.getSwitchRequest() != null && q.getSwitchRequest().isConfirmed())
          .select(q -> q.getPlane());
      return ret;
    }

    public void confirmSwitch(Airplane plane, Atc targetAtc) {
      AirplaneResponsibilityInfo ai = dao.get(plane);
      if (ai.getSwitchRequest() == null || ai.getSwitchRequest().getAtc() != targetAtc) { // probably canceled
        return;
      }
      SwitchRequest sr = ai.getSwitchRequest();
      sr.setConfirmed();
    }

    public void rejectSwitch(Airplane plane, Atc targetAtc) {
      AirplaneResponsibilityInfo ai = dao.get(plane);
      if (ai.getSwitchRequest() == null || ai.getSwitchRequest().getAtc() != targetAtc) { // probably canceled
        return;
      }
      ai.setSwitchRequest(null);
    }

    public void cancelSwitchRequest(Atc sender, Airplane plane) {
      AirplaneResponsibilityInfo ai = dao.get(plane);
      ai.setSwitchRequest(null);
    }

    public void applyConfirmedSwitch(Atc sender, Airplane plane) {
      AirplaneResponsibilityInfo ai = dao.get(plane);
      if (ai.getSwitchRequest() == null || ai.getAtc() != sender) { // probably canceled
        return;
      }
      SwitchRequest sr = ai.getSwitchRequest();

      ai.getAtc().unregisterPlaneUnderControl(plane);
      ai.setAtc(sr.getAtc());
      ai.getAtc().registerNewPlaneUnderControl(plane, false);
      ai.setSwitchRequest(null);
    }
  }

  private PlaneResponsibilityManagerForAtc forAtc = new PlaneResponsibilityManagerForAtc();
  private PlaneResponsibilityDAO dao = new PlaneResponsibilityDAO();

  public PlaneResponsibilityManager() {
  }

  public PlaneResponsibilityManagerForAtc forAtc() {
    return this.forAtc;
  }

  public void init() {
    dao.init();
  }

  public Atc getResponsibleAtc(Airplane plane) {
    Atc ret;
    AirplaneResponsibilityInfo ai = dao.get(plane);
    ret = ai.getAtc();
    return ret;
  }

  public void registerNewPlane(Atc atc, Airplane plane) {
    if (dao.getAll().isAny(q -> q.getPlane() == plane)) {
      throw new EApplicationException(sf("Second registration of already registered plane %s!", plane.getCallsign()));
    }

    dao.add(new AirplaneResponsibilityInfo(plane, atc));
    atc.registerNewPlaneUnderControl(plane, true);
  }

  public void unregisterPlane(Airplane plane) {
    AirplaneResponsibilityInfo ai = dao.getAll().tryGetFirst(q -> q.getPlane() == plane);
    if (ai == null) {
      throw new EApplicationException(sf("Plane %s is not registered, cannot be unregistered!", plane.getCallsign()));
    }
    dao.remove(ai);
    ai.getAtc().removePlaneDeletedFromGame(plane);
//    Acc.atcApp().removePlaneDeletedFromGame(plane);
//    Acc.atcTwr().removePlaneDeletedFromGame(plane);
//    Acc.atcCtr().removePlaneDeletedFromGame(plane);
  }

  public IReadOnlyList<Airplane.Airplane4Display> getPlanesToDisplay() {
    IReadOnlyList<Airplane.Airplane4Display> ret = dao.getDisplays();
    return ret;
  }

  public IReadOnlyList<Airplane> getPlanes() {
    return dao.getAll().select(q -> q.getPlane());
  }

  public boolean isUnderConfirmedSwitch(Callsign callsign) {
    AirplaneResponsibilityInfo ari = dao.get(callsign);
    boolean ret = ari.getSwitchRequest() != null && ari.getSwitchRequest().isConfirmed();
    return ret;
  }

//  public void applySwitch(Airplane plane, Atc oldAtc) {
//
//  }
//
//  public void abortSwitch(Airplane plane) {
//    AirplaneInfo ai = all.getFirst(q -> q.getPlane() == plane);
//    if (ai.getSwitchRequest() == null)
//      throw new EApplicationException("Unable to abort switch of " + plane.getCallsign().toString() + " as it is not under switch request.");
//
//
//    ai.setSwitchRequest(null);
//  }
//


//  public void save(XElement elm) {
///*
//  private final IMap<Airplane, eState> map = new EMap<>();
//  private final IMap<Atc, AirplaneList> lst = new EMap<>();
//  private final AirplaneList all = new AirplaneList();
// */
//
//    XmlSerializer ser = new XmlSerializer();
//
//    XElement tmp;
//
//    try {
//
//      throw new UnsupportedOperationException();
//
////      tmp = new XElement("planes");
////      for (Airplane airplane : all) {
////        XElement tmpAp = new XElement("plane");
////        airplane.save(tmpAp);
////        tmp.addElement(tmpAp);
////      }
////
////      tmp = new XElement("states");
////      IMap<String, String> remap = map.select(q -> q.getCallsign().toString(), q -> q.toString());
////      ser.serialize(tmp, remap);
////      elm.addElement(tmp);
////
////      tmp = new XElement("atc");
////      IMap<String, IList<String>> relst = lst.select(q -> q.getName(), q -> q.select(o -> o.getCallsign().toString()));
////      ser.serialize(tmp, relst);
////      elm.addElement(tmp);
//
//      // displays not serialized, should be created during deserialization
//
//    } catch (XmlSerializationException e) {
//      throw new EApplicationException("Failed to store PlaneResponsibilityManager.", e);
//    }
//  }


}
