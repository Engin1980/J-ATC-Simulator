/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.newLib.atcs.planeResponsibility;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.ToDoException;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.IAirplane;
import eng.jAtcSim.newLib.atcs.contextLocal.Context;
import eng.jAtcSim.newLib.atcs.internal.Atc;
import eng.jAtcSim.newLib.atcs.internal.InternalAcc;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.enums.AtcType;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class PlaneResponsibilityManager {

  public class PlaneResponsibilityManagerForAtc {

    public IReadOnlyList<Callsign> getPlanes(AtcId atc) {
      IReadOnlyList<AirplaneResponsibilityInfo> tmp = dao.getByAtc(atc);
      IReadOnlyList<Callsign> ret = tmp.select(q -> q.getPlane());
      return ret;
    }

    public boolean isUnderSwitchRequest(Callsign plane, AtcId sourceAtc, AtcId targetAtc) {
      boolean ret;
      AirplaneResponsibilityInfo ai = dao.get(plane);
      ret = ai.getSwitchRequest() != null;
      if (ret && sourceAtc != null)
        ret = ai.getAtc().equals(sourceAtc);
      if (ret && targetAtc != null)
        ret = ai.getSwitchRequest().getAtc().equals(targetAtc);
      return ret;
    }

    public AtcId getResponsibleAtc(Callsign plane) {
      return PlaneResponsibilityManager.this.getResponsibleAtc(plane);
    }

    public IReadOnlyList<Callsign> getSwitchRequestsToRepeatByAtc(AtcId sender) {
      IReadOnlyList<AirplaneResponsibilityInfo> tmp = dao.getByAtc(sender);
      tmp = tmp.where(q -> q.getSwitchRequest() != null
          && q.getAtc() == sender
          && q.getSwitchRequest().isConfirmed() == false
          && q.getSwitchRequest().getRepeatRequestTime().isBefore(Context.getShared().getNow().toStamp()));
      tmp.forEach(q -> q.getSwitchRequest().updateLastRequestTime());
      IReadOnlyList<Callsign> ret = tmp.select(q -> q.getPlane());
      return ret;
    }

    public void createSwitchRequest(AtcId sender, AtcId targetAtc, Callsign callsign) {
      EAssert.Argument.isNotNull(sender);
      EAssert.Argument.isNotNull(targetAtc);
      EAssert.Argument.isNotNull(callsign);

      AirplaneResponsibilityInfo ai = dao.get(callsign);

      IAirplane plane = InternalAcc.getPlane(ai.getPlane());
      Atc atc = InternalAcc.getAtc(ai.getAtc());

      // auto-cancel
      if (ai.getAtc() == targetAtc && plane.getAtc().getTunedAtc() == sender) {
        ai.setAtc(sender);
        ai.setSwitchRequest(null);
        return;
      }

      if (ai.getSwitchRequest() != null)
        throw new EApplicationException("Airplane " + plane.getCallsign() + " is already under request switch from "
            + atc.getAtcId().getType().toString() + " to " + ai.getSwitchRequest().getAtc().getType().toString() + ".");
      if (ai.getAtc().equals(sender) == false)
        throw new EApplicationException("Airplane " + plane.getCallsign()
            + " is requested to be switched from incorrect atc. Current is "
            + atc.getAtcId().getType().toString() + ", requested from is " + sender.getType().toString() + ".");

      if ((sender.getType() == AtcType.ctr || sender.getType() == AtcType.twr)
          && targetAtc.getType() != AtcType.app)
        throw new EApplicationException("Invalid request direction.");

      SwitchRequest sr = new SwitchRequest(targetAtc);
      ai.setSwitchRequest(sr);
    }

    public IReadOnlyList<Callsign> getConfirmedSwitchesByAtc(AtcId sender, boolean excludeWithRerouting) {
      IReadOnlyList<AirplaneResponsibilityInfo> tmp = dao.getByAtc(sender);
       tmp = tmp
          .where(q -> q.getSwitchRequest() != null && q.getSwitchRequest().isConfirmed());
       if (excludeWithRerouting)
         tmp = tmp.where(q->q.getSwitchRequest().getRouting() == null);
      IList<Callsign> ret = tmp.select(q->q.getPlane());
      return ret;

    }

    public void confirmSwitchRequest(Callsign callsign, AtcId targetAtcId, SwitchRoutingRequest updatedRoutingIfRequired) {
      AirplaneResponsibilityInfo ai = dao.get(callsign);
      if (ai.getSwitchRequest() == null || ai.getSwitchRequest().getAtc() != targetAtcId) { // probably canceled
        return;
      }
      SwitchRequest sr = ai.getSwitchRequest();
      sr.setConfirmed(updatedRoutingIfRequired);
    }

    public void rejectSwitchRequest(Callsign callsign, AtcId targetAtcId) {
      AirplaneResponsibilityInfo ai = dao.get(callsign);
      if (ai.getSwitchRequest() == null || ai.getSwitchRequest().getAtc() != targetAtcId) { // probably canceled
        return;
      }
      ai.setSwitchRequest(null);
    }

    public void cancelSwitchRequest(AtcId sender, Callsign callsign) {
      AirplaneResponsibilityInfo ai = dao.get(callsign);
      ai.setSwitchRequest(null);
    }

    public void applyConfirmedSwitch(AtcId sender, Callsign callsign) {
      AirplaneResponsibilityInfo ai = dao.get(callsign);
      if (ai.getSwitchRequest() == null || ai.getAtc() != sender) { // probably canceled
        return;
      }
      SwitchRequest sr = ai.getSwitchRequest();

      Atc atc;

      atc = InternalAcc.getAtc(ai.getAtc());
      atc.unregisterPlaneUnderControl(callsign);
      ai.setAtc(sr.getAtc());

      atc = InternalAcc.getAtc(ai.getAtc());
      atc.registerNewPlaneUnderControl(callsign, false);
      ai.setSwitchRequest(null);
    }

    public SwitchRoutingRequest getRoutingForSwitchRequest(AtcId sender, Callsign callsign) {
      AirplaneResponsibilityInfo ari = dao.get(callsign);
      SwitchRequest sr = ari.getSwitchRequest();
      SwitchRoutingRequest srr = sr.getRouting();
      return srr;
    }

    public void resetSwitchRequest(AtcId sender, Callsign callsign) {
      AirplaneResponsibilityInfo ari = dao.get(callsign);
      SwitchRequest sr = ari.getSwitchRequest();

      assert ari.getAtc() == sender;

      sr.reset();
    }

    public void confirmRerouting(AtcId sender, Callsign callsign) {
      AirplaneResponsibilityInfo ari = dao.get(callsign);
      SwitchRequest sr = ari.getSwitchRequest();

      assert ari.getAtc() == sender;

      //FIXME update this
      throw new ToDoException("This must be rewritten without usage of writing to plane.");
//      SwitchRoutingRequest srr = sr.getRouting();
//      Airplane fullPlane = PlaneResponsibilityManager.this.getPlanes().getFirst(q->q == callsign);
//      fullPlane.getAdvanced().setRouting(srr.route, srr.threshold);
//      sr.deleteConfirmedRouting();
    }
  }

  //TODO PSR build based on squawks and not callsigns as ATC uses them that way and now they need
  // to recode sqwk to callsign and back repeately
  private final PlaneResponsibilityManagerForAtc forAtc = new PlaneResponsibilityManagerForAtc();
  private final PlaneResponsibilityDAO dao = new PlaneResponsibilityDAO();

  public PlaneResponsibilityManager() {
  }

  public PlaneResponsibilityManagerForAtc forAtc() {
    return this.forAtc;
  }

  public AtcId getResponsibleAtc(Callsign callsign) {
    AtcId ret;
    AirplaneResponsibilityInfo ai = dao.get(callsign);
    ret = ai.getAtc();
    return ret;
  }

  public void registerNewPlane(AtcId atcId, Callsign callsign) {
    if (dao.getAll().isAny(q -> q.getPlane() == callsign)) {
      throw new EApplicationException(sf(
          "Second registration of already registered plane %s!",
          callsign));
    }

    dao.add(new AirplaneResponsibilityInfo(callsign, atcId));
  }

  public void unregisterPlane(Callsign callsign) {
    AirplaneResponsibilityInfo ai = dao.getAll().tryGetFirst(q -> q.getPlane() == callsign);
    if (ai == null) {
      throw new EApplicationException(sf(
          "Plane %s is not registered, cannot be unregistered!",
          callsign));
    }
    dao.remove(ai);

    Atc atc = InternalAcc.getAtc(ai);
    atc.removePlaneDeletedFromGame(callsign);
//    Acc.atcApp().removePlaneDeletedFromGame(plane);
//    Acc.atcTwr().removePlaneDeletedFromGame(plane);
//    Acc.atcCtr().removePlaneDeletedFromGame(plane);
  }

//  public IReadOnlyList<Airplane.Airplane4Display> getPlanesToDisplay() {
//    IReadOnlyList<Airplane.Airplane4Display> ret = dao.getDisplays();
//    return ret;
//  }

  public IReadOnlyList<Callsign> getPlanes() {
    return dao.getAll().select(q -> q.getPlane());
  }

  public boolean isUnderConfirmedSwitch(Callsign callsign) {
    AirplaneResponsibilityInfo ari = dao.get(callsign);
    boolean ret = ari.getSwitchRequest() != null && ari.getSwitchRequest().isConfirmed();
    return ret;
  }

}
