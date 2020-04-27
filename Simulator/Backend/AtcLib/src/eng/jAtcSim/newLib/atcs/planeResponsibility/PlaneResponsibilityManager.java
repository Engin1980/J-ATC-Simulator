/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.newLib.atcs.planeResponsibility;

import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.GAcc;

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
        ret = ai.getAtc() == sourceAtc;
      if (ret && targetAtc != null)
        ret = ai.getSwitchRequest().getAtc() == targetAtc;
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
          && q.getSwitchRequest().getRepeatRequestTime().isBefore(GAcc.getNow()));
      tmp.forEach(q -> q.getSwitchRequest().updateLastRequestTime());
      IReadOnlyList<Callsign> ret = tmp.select(q -> q.getPlane());
      return ret;
    }

    public void createSwitchRequest(AtcId sender, AtcId targetAtc, Callsign plane) {
      EAssert.Argument.isNotNull(sender);
      EAssert.Argument.isNotNull(targetAtc);
      EAssert.Argument.isNotNull(plane);

      AirplaneResponsibilityInfo ai = dao.get(plane);

      // auto-cancel
      if (ai.getAtc() == targetAtc && ai.getPlane().getAtcModule().getTunedAtc() == sender) {
        ai.setAtc(sender);
        ai.setSwitchRequest(null);
        return;
      }

      if (ai.getSwitchRequest() != null)
        throw new EApplicationException("Airplane " + plane.getFlightModule().getCallsign() + " is already under request switch from "
            + ai.getAtc().getType().toString() + " to " + ai.getSwitchRequest().getAtc().getType().toString() + ".");
      if (ai.getAtc() != sender)
        throw new EApplicationException("Airplane " + plane.getFlightModule().getCallsign()
            + " is requested to be switched from incorrect atc. Current is "
            + ai.getAtc().getType().toString() + ", requested from is " + sender.getType().toString() + ".");

      if ((sender.getType() == Atc.eType.ctr || sender.getType() == Atc.eType.twr) && targetAtc.getType() != Atc.eType.app)
        throw new EApplicationException("Invalid request direction.");

      SwitchRequest sr = new SwitchRequest(targetAtc);
      ai.setSwitchRequest(sr);
    }

    public IReadOnlyList<IAirplane4Atc> getConfirmedSwitchesByAtc(AtcId sender, boolean excludeWithRerouting) {
      IReadOnlyList<AirplaneResponsibilityInfo> tmp = dao.getByAtc(sender);
       tmp = tmp
          .where(q -> q.getSwitchRequest() != null && q.getSwitchRequest().isConfirmed());
       if (excludeWithRerouting)
         tmp = tmp.where(q->q.getSwitchRequest().getRouting() == null);
      IList<IAirplane4Atc> ret = tmp.select(q->q.getPlane());
      return ret;

    }

    public void confirmSwitchRequest(Callsign plane, AtcId targetAtc, @Nullable SwitchRoutingRequest updatedRoutingIfRequired) {
      AirplaneResponsibilityInfo ai = dao.get(plane);
      if (ai.getSwitchRequest() == null || ai.getSwitchRequest().getAtc() != targetAtc) { // probably canceled
        return;
      }
      SwitchRequest sr = ai.getSwitchRequest();
      sr.setConfirmed(updatedRoutingIfRequired);
    }

    public void rejectSwitchRequest(Callsign plane, AtcId targetAtc) {
      AirplaneResponsibilityInfo ai = dao.get(plane);
      if (ai.getSwitchRequest() == null || ai.getSwitchRequest().getAtc() != targetAtc) { // probably canceled
        return;
      }
      ai.setSwitchRequest(null);
    }

    public void cancelSwitchRequest(AtcId sender, Callsign plane) {
      AirplaneResponsibilityInfo ai = dao.get(plane);
      ai.setSwitchRequest(null);
    }

    public void applyConfirmedSwitch(AtcId sender, Callsign plane) {
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

    public SwitchRoutingRequest getRoutingForSwitchRequest(AtcId sender, Callsign plane) {
      AirplaneResponsibilityInfo ari = dao.get(plane);
      SwitchRequest sr = ari.getSwitchRequest();
      SwitchRoutingRequest srr = sr.getRouting();
      return srr;
    }

    public void resetSwitchRequest(AtcId sender, Callsign plane) {
      AirplaneResponsibilityInfo ari = dao.get(plane);
      SwitchRequest sr = ari.getSwitchRequest();

      assert ari.getAtc() == sender;

      sr.reset();
    }

    public void confirmRerouting(AtcId sender, Callsign plane) {
      AirplaneResponsibilityInfo ari = dao.get(plane);
      SwitchRequest sr = ari.getSwitchRequest();

      assert ari.getAtc() == sender;

      SwitchRoutingRequest srr = sr.getRouting();
      Airplane fullPlane = PlaneResponsibilityManager.this.getPlanes().getFirst(q->q == plane);
      fullPlane.getAdvanced().setRouting(srr.route, srr.threshold);
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

  public AtcId getResponsibleAtc(Callsign plane) {
    Atc ret;
    AirplaneResponsibilityInfo ai = dao.get(plane);
    ret = ai.getAtc();
    return ret;
  }

  public void registerNewPlane(AtcId atc, Callsign plane) {
    if (dao.getAll().isAny(q -> q.getPlane() == plane)) {
      throw new EApplicationException(sf("Second registration of already registered plane %s!", plane.getFlightModule().getCallsign()));
    }

    dao.add(new AirplaneResponsibilityInfo(plane, atc));
    atc.registerNewPlaneUnderControl(plane, true);
  }

  public void unregisterPlane(Callsign plane) {
    AirplaneResponsibilityInfo ai = dao.getAll().tryGetFirst(q -> q.getPlane() == plane);
    if (ai == null) {
      throw new EApplicationException(sf("Plane %s is not registered, cannot be unregistered!", plane.getFlightModule().getCallsign()));
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

  public IReadOnlyList<Callsign> getPlanes() {
    return dao.getAll().select(q -> q.getPlane());
  }

  public boolean isUnderConfirmedSwitch(Callsign callsign) {
    AirplaneResponsibilityInfo ari = dao.get(callsign);
    boolean ret = ari.getSwitchRequest() != null && ari.getSwitchRequest().isConfirmed();
    return ret;
  }

}
