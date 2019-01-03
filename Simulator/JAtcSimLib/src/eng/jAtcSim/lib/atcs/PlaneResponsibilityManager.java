/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.atcs;

import com.sun.istack.internal.Nullable;
import eng.eSystem.collections.*;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.validation.Validator;
import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.AirplaneList;
import eng.jAtcSim.lib.global.ETime;

import java.util.LinkedList;
import java.util.List;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class PlaneResponsibilityManager {

  private static class AirplaneInfo {
    private Airplane plane;
    private Atc atc;
    private SwitchRequest switchRequest;

    public AirplaneInfo(Airplane plane, Atc atc) {
      Validator.isNotNull(plane);
      Validator.isNotNull(atc);

      this.plane = plane;
      this.atc = atc;
      this.switchRequest = null;
    }

    public Airplane getPlane() {
      return plane;
    }

    public Atc getAtc() {
      return atc;
    }

    void setAtc(Atc atc) {
      Validator.isNotNull(atc);
      this.atc = atc;
    }

    public SwitchRequest getSwitchRequest() {
      return switchRequest;
    }

    void setSwitchRequest(SwitchRequest switchRequest) {
      this.switchRequest = switchRequest;
    }
  }

  public static class SwitchRequest {
    private ETime creationTime;
    private Atc atc;
    private ETime repeatRequestTime;
    private ETime confirmedTime = null;

    public void setConfirmed(){
      this.confirmedTime = Acc.now().clone();
    }

    public SwitchRequest(Atc atc) {
      this.atc = atc;
      this.creationTime = Acc.now().clone();
      this.updateLastRequestTime();
    }

    public ETime getRepeatRequestTime() {
      return repeatRequestTime;
    }

    public ETime getCreationTime() {
      return creationTime;
    }

    public Atc getAtc() {
      return atc;
    }

    void confirm(Atc oldAtc) {
      this.atc = oldAtc;
    }

    public ETime getConfirmedTime() {
      return confirmedTime;
    }

    public boolean isConfirmed(){
      return this.confirmedTime != null;
    }

    public void updateLastRequestTime(){
      this.repeatRequestTime = Acc.now().addSeconds(30);
    }
  }

  private final IList<AirplaneInfo> all = new EList<>();
  @XmlIgnore
  private final List<Airplane.Airplane4Display> displays = new LinkedList<>();
  private boolean VERBOSE = true;

  public PlaneResponsibilityManager() {
  }

  public void init() {
    for (AirplaneInfo ai : all) {
      displays.add(ai.getPlane().getPlane4Display());
    }
  }

  public ReadOnlyList<Airplane.Airplane4Display> getPlanesToDisplay() {
    ReadOnlyList<Airplane.Airplane4Display> ret
        = new ReadOnlyList<>(this.displays);
    return ret;
  }

  public void registerPlane(Atc atc, Airplane plane) {
    if (all.isAny(q -> q.getPlane() == plane)) {
      throw new EApplicationException(sf("Second registration of already registered plane %s!", plane.getCallsign()));
    }

    all.add(new AirplaneInfo(plane, atc));
    displays.add(plane.getPlane4Display());
    atc.registerNewPlaneUnderControl(plane, true);
  }

  public void unregisterPlane(Airplane plane) {
    AirplaneInfo ai = all.tryGetFirst(q -> q.plane == plane);
    if (ai == null) {
      throw new EApplicationException(sf("Plane %s is not registered, cannot be unregistered!", plane.getCallsign()));
    }
    all.remove(ai);
    displays.remove(ai.getPlane().getPlane4Display());
    ai.getAtc().removePlaneDeletedFromGame(plane);
//    Acc.atcApp().removePlaneDeletedFromGame(plane);
//    Acc.atcTwr().removePlaneDeletedFromGame(plane);
//    Acc.atcCtr().removePlaneDeletedFromGame(plane);
  }

  public void requestSwitch(Atc from, Atc to, Airplane plane) {
    Validator.isNotNull(from);
    Validator.isNotNull(to);
    Validator.isNotNull(plane);

    AirplaneInfo ai = all.getFirst(q -> q.getPlane() == plane);

    // auto-cancel
    if (ai.getAtc() == to && ai.getPlane().getTunedAtc() == from) {
      ai.setAtc(from);
      ai.setSwitchRequest(null);
      return;
    }

    if (ai.getSwitchRequest() != null)
      throw new EApplicationException("Airplane " + plane.getCallsign() + " is already under request switch from "
          + ai.getAtc().getType().toString() + " to " + ai.getSwitchRequest().getAtc().getType().toString() + ".");
    if (ai.getAtc() != from)
      throw new EApplicationException("Airplane " + plane.getCallsign()
          + " is requested to be switched from incorrect atc. Current is "
          + ai.getAtc().getType().toString() + ", requested from is " + from.getType().toString() + ".");

    if ((from.getType() == Atc.eType.ctr || from.getType() == Atc.eType.twr) && to.getType() != Atc.eType.app)
      throw new EApplicationException("Invalid request direction.");

    SwitchRequest sr = new SwitchRequest(to);
    ai.setSwitchRequest(sr);
    verb(plane.getCallsign() + " request from " + from.getType() + " to " + to.getType());
  }

  public void confirmSwitch(Atc atc, Airplane plane) {
    AirplaneInfo ai = all.getFirst(q -> q.getPlane() == plane);
    if (ai.getSwitchRequest() == null || ai.getSwitchRequest().getAtc() != atc) { // probably canceled
      verb(plane.getCallsign() + " confirmation failed, no request or not from " + atc.getType() + ".");
      return;
    }
    SwitchRequest sr = ai.getSwitchRequest();
    sr.setConfirmed();
  }

  public void applySwitch(Airplane plane, Atc oldAtc){
    AirplaneInfo ai = all.getFirst(q -> q.getPlane() == plane);
    if (ai.getSwitchRequest() == null || ai.getAtc() != oldAtc) { // probably canceled
      verb(plane.getCallsign() + " switch application failed, no request or not from " + oldAtc.getType() + ".");
      return;
    }
    SwitchRequest sr = ai.getSwitchRequest();

    ai.getAtc().unregisterPlaneUnderControl(plane);
    ai.setAtc(sr.getAtc());
    ai.getAtc().registerNewPlaneUnderControl(plane, false);
    ai.setSwitchRequest(null);
  }

  public void abortSwitch(Airplane plane) {
    AirplaneInfo ai = all.getFirst(q -> q.getPlane() == plane);
    if (ai.getSwitchRequest() == null)
      throw new EApplicationException("Unable to abort switch of " + plane.getCallsign().toString() + " as it is not under switch request.");


    ai.setSwitchRequest(null);
  }

  public boolean isToSwitchToAtc(Atc targetAtc, Airplane plane) {
    AirplaneInfo ai = all.getFirst(q -> q.getPlane() == plane);
    SwitchRequest sr = ai.getSwitchRequest();
    return sr != null && sr.getAtc() == targetAtc;
  }

//  public boolean isUnderConfirmedSwitchRequest(Airplane plane) {
//    AirplaneInfo ai = all.getFirst(q -> q.getPlane() == plane);
//    boolean ret = ai.getSwitchRequest() != null && ai.getSwitchRequest().isConfirmed();
//    return ret;
//  }

  @Deprecated()
  public boolean isRequestedToSwitch(Airplane plane, Atc atc) {
    boolean ret = isToSwitchToAtc(atc, plane);
    return ret;
  }

  public Atc getResponsibleAtc(Airplane plane) {
    AirplaneInfo ai = all.getFirst(q -> q.getPlane() == plane);
    return ai.getAtc();
  }

  public IReadOnlyList<Airplane> getSwitchRequestsToRepeatByAtc(Atc atc) {
    IReadOnlyList<AirplaneInfo> tmp =
        this.all.where(q->q.getSwitchRequest() != null
            && q.getAtc() == atc
            && q.getSwitchRequest().isConfirmed() == false
            && q.getSwitchRequest().getRepeatRequestTime().isBefore(Acc.now()));
    tmp.forEach(q->q.getSwitchRequest().updateLastRequestTime());
    IReadOnlyList<Airplane> ret = tmp.select(q->q.getPlane());
    return ret;
  }


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

  private void verb(String line) {
    if (VERBOSE)
      System.out.println("## PRM ##: " + line);
  }

  protected IReadOnlyList<Airplane> getPlanes(Atc atc) {
    return all.where(q->q.getAtc() == atc).select(q -> q.getPlane());
  }

  public IReadOnlyList<Airplane> getPlanes() {
    return all.select(q -> q.getPlane());
  }

  public boolean xisUnderSwitchRequest(Airplane plane, @Nullable Atc sourceAtc, @Nullable Atc targetAtc){
    boolean ret;
    AirplaneInfo ai = all.getFirst(q -> q.getPlane() == plane);
    ret = ai.getSwitchRequest() != null;
    if (ret && sourceAtc != null)
      ret = ai.getAtc() == sourceAtc;
    if (ret && targetAtc != null)
      ret = ai.getSwitchRequest().getAtc() == targetAtc;
    return ret;
  }
}
