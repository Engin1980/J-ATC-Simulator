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
import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.Callsign;
import eng.jAtcSim.lib.airplanes.pilots.interfaces.forAirplane.IAirplaneRO;
import eng.jAtcSim.lib.atcs.Atc;
import eng.jAtcSim.lib.atcs.ComputerAtc;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class PlaneResponsibilityManager {

  public class PlaneResponsibilityManagerForAtc {

    public IReadOnlyList<IAirplaneRO> getPlanes(Atc atc) {
      IReadOnlyList<AirplaneResponsibilityInfo> tmp = dao.getByAtc(atc);
      IReadOnlyList<IAirplaneRO> ret = tmp.select(q -> q.getPlane());
      return ret;
    }

    public boolean isUnderSwitchRequest(IAirplaneRO plane, @Nullable Atc sourceAtc, @Nullable Atc targetAtc) {
      boolean ret;
      AirplaneResponsibilityInfo ai = dao.get(plane);
      ret = ai.getSwitchRequest() != null;
      if (ret && sourceAtc != null)
        ret = ai.getAtc() == sourceAtc;
      if (ret && targetAtc != null)
        ret = ai.getSwitchRequest().getAtc() == targetAtc;
      return ret;
    }

    public Atc getResponsibleAtc(IAirplaneRO plane) {
      return PlaneResponsibilityManager.this.getResponsibleAtc(plane);
    }

    public IReadOnlyList<IAirplaneRO> getSwitchRequestsToRepeatByAtc(Atc sender) {
      IReadOnlyList<AirplaneResponsibilityInfo> tmp = dao.getByAtc(sender);
      tmp = tmp.where(q -> q.getSwitchRequest() != null
          && q.getAtc() == sender
          && q.getSwitchRequest().isConfirmed() == false
          && q.getSwitchRequest().getRepeatRequestTime().isBefore(Acc.now()));
      tmp.forEach(q -> q.getSwitchRequest().updateLastRequestTime());
      IReadOnlyList<IAirplaneRO> ret = tmp.select(q -> q.getPlane());
      return ret;
    }

    public void createSwitchRequest(Atc sender, Atc targetAtc, Airplane plane) {
      Validator.isNotNull(sender);
      Validator.isNotNull(targetAtc);
      Validator.isNotNull(plane);

      AirplaneResponsibilityInfo ai = dao.get(plane);

      // auto-cancel
      if (ai.getAtc() == targetAtc && ai.getPlane().getPilot().getAtcModule().getTunedAtc() == sender) {
        ai.setAtc(sender);
        ai.setSwitchRequest(null);
        return;
      }

      if (ai.getSwitchRequest() != null)
        throw new EApplicationException("Airplane " + plane.getFlight().getCallsign() + " is already under request switch from "
            + ai.getAtc().getType().toString() + " to " + ai.getSwitchRequest().getAtc().getType().toString() + ".");
      if (ai.getAtc() != sender)
        throw new EApplicationException("Airplane " + plane.getFlight().getCallsign()
            + " is requested to be switched from incorrect atc. Current is "
            + ai.getAtc().getType().toString() + ", requested from is " + sender.getType().toString() + ".");

      if ((sender.getType() == Atc.eType.ctr || sender.getType() == Atc.eType.twr) && targetAtc.getType() != Atc.eType.app)
        throw new EApplicationException("Invalid request direction.");

      SwitchRequest sr = new SwitchRequest(targetAtc);
      ai.setSwitchRequest(sr);
    }

    public IReadOnlyList<IAirplaneRO> getConfirmedSwitchesByAtc(Atc sender, boolean excludeWithRerouting) {
      IReadOnlyList<AirplaneResponsibilityInfo> tmp = dao.getByAtc(sender);
       tmp = tmp
          .where(q -> q.getSwitchRequest() != null && q.getSwitchRequest().isConfirmed());
       if (excludeWithRerouting)
         tmp = tmp.where(q->q.getSwitchRequest().getRouting() == null);
      IList<IAirplaneRO> ret = tmp.select(q->q.getPlane());
      return ret;

    }

    public void confirmSwitchRequest(Airplane plane, Atc targetAtc, @Nullable SwitchRoutingRequest updatedRoutingIfRequired) {
      AirplaneResponsibilityInfo ai = dao.get(plane);
      if (ai.getSwitchRequest() == null || ai.getSwitchRequest().getAtc() != targetAtc) { // probably canceled
        return;
      }
      SwitchRequest sr = ai.getSwitchRequest();
      sr.setConfirmed(updatedRoutingIfRequired);
    }

    public void rejectSwitchRequest(IAirplaneRO plane, Atc targetAtc) {
      AirplaneResponsibilityInfo ai = dao.get(plane);
      if (ai.getSwitchRequest() == null || ai.getSwitchRequest().getAtc() != targetAtc) { // probably canceled
        return;
      }
      ai.setSwitchRequest(null);
    }

    public void cancelSwitchRequest(Atc sender, IAirplaneRO plane) {
      AirplaneResponsibilityInfo ai = dao.get(plane);
      ai.setSwitchRequest(null);
    }

    public void applyConfirmedSwitch(Atc sender, IAirplaneRO plane) {
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

    public SwitchRoutingRequest getRoutingForSwitchRequest(Atc sender, IAirplaneRO plane) {
      AirplaneResponsibilityInfo ari = dao.get(plane);
      SwitchRequest sr = ari.getSwitchRequest();
      SwitchRoutingRequest srr = sr.getRouting();
      return srr;
    }

    public void resetSwitchRequest(ComputerAtc sender, IAirplaneRO plane) {
      AirplaneResponsibilityInfo ari = dao.get(plane);
      SwitchRequest sr = ari.getSwitchRequest();

      assert ari.getAtc() == sender;

      sr.reset();
    }

    public void confirmRerouting(ComputerAtc sender, IAirplaneRO plane) {
      AirplaneResponsibilityInfo ari = dao.get(plane);
      SwitchRequest sr = ari.getSwitchRequest();

      assert ari.getAtc() == sender;

      SwitchRoutingRequest srr = sr.getRouting();
      plane.updateAssignedRouting(srr.route, srr.threshold);
      sr.deleteConfirmedRouting();
    }
  }

  @XmlIgnore
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

  public Atc getResponsibleAtc(IAirplaneRO plane) {
    Atc ret;
    AirplaneResponsibilityInfo ai = dao.get(plane);
    ret = ai.getAtc();
    return ret;
  }

  public void registerNewPlane(Atc atc, IAirplaneRO plane) {
    if (dao.getAll().isAny(q -> q.getPlane() == plane)) {
      throw new EApplicationException(sf("Second registration of already registered plane %s!", plane.getFlight().getCallsign()));
    }

    dao.add(new AirplaneResponsibilityInfo(plane, atc));
    atc.registerNewPlaneUnderControl(plane, true);
  }

  public void unregisterPlane(Airplane plane) {
    AirplaneResponsibilityInfo ai = dao.getAll().tryGetFirst(q -> q.getPlane() == plane);
    if (ai == null) {
      throw new EApplicationException(sf("Plane %s is not registered, cannot be unregistered!", plane.getFlight().getCallsign()));
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

  public IReadOnlyList<IAirplaneRO> getPlanes() {
    return dao.getAll().select(q -> q.getPlane());
  }

  public boolean isUnderConfirmedSwitch(Callsign callsign) {
    AirplaneResponsibilityInfo ari = dao.get(callsign);
    boolean ret = ari.getSwitchRequest() != null && ari.getSwitchRequest().isConfirmed();
    return ret;
  }

}
