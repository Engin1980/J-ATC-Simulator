package eng.jAtcSim.newLib.airplanes;

import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.ApplicationException;
import eng.jAtcSim.newLib.airplanes.contextLocal.Context;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;
import eng.jAtcSim.newLib.airplanes.templates.AirplaneTemplate;
import eng.jAtcSim.newLib.airplanes.templates.ArrivalAirplaneTemplate;
import eng.jAtcSim.newLib.airplanes.templates.DepartureAirplaneTemplate;
import eng.jAtcSim.newLib.messaging.Participant;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.PostContracts;
import eng.jAtcSim.newLib.shared.Squawk;
import eng.jAtcSim.newLib.shared.enums.AtcType;
import exml.IXPersistable;
import exml.loading.XLoadContext; import exml.saving.XSaveContext;
import exml.annotations.XIgnored;

public class AirplanesController implements IXPersistable {

  private final AirplaneList planes = new AirplaneList();
  @XIgnored private final IAirplaneList publicPlanes = new IAirplaneList();
  @XIgnored private AtcId departureInitialAtcId;
  @XIgnored private AtcId arrivalInitialAtId;

  public AirplanesController() {
    PostContracts.register(this, () -> planes.size() == publicPlanes.size(), "Planes vs. publicPlanes does not match.");
  }

  public IAirplaneList getPlanes() {
    return publicPlanes;
  }

  public void init() {
    this.arrivalInitialAtId = Context.getShared().getAtcs().getFirst(q -> q.getType() == AtcType.ctr);
    this.departureInitialAtcId = Context.getShared().getAtcs().getFirst(q -> q.getType() == AtcType.twr);

    //TODO move this to load()?
    for (Airplane plane : planes) {
      Context.getMessaging().getMessenger().registerListener(
              Participant.createAirplane(plane.getReader().getCallsign()));
    }
  }

  public IAirplane registerPlane(AirplaneTemplate at, Squawk sqwk) {
    Airplane airplane;

    if (at instanceof DepartureAirplaneTemplate) {
      airplane = Airplane.createDeparture((DepartureAirplaneTemplate) at, sqwk, departureInitialAtcId);
    } else if (at instanceof ArrivalAirplaneTemplate) {
      airplane = Airplane.createArrival((ArrivalAirplaneTemplate) at, sqwk, arrivalInitialAtId);
    } else
      throw new ApplicationException("Unknown airplane template type " + at.getClass().getName());

    this.planes.add(airplane);
    this.publicPlanes.add(airplane.getReader());

    return airplane.getReader();
  }

  @Override
  public void xSave(XElement elm, XSaveContext ctx) {
    ctx.fields.saveFieldItems(this, "planes", Airplane.class, elm);
  }

  @Override
  public void xLoad(XElement elm, XLoadContext ctx) {
    ctx.fields.loadFieldItems(this, "planes", this.planes, Airplane.class, elm);
    this.publicPlanes.addMany(this.planes.select(q->q.getReader()));
  }

  public void throwEmergency() {
    this.planes
      .where(q -> q.getReader().getState().is(AirplaneState.departingLow,
              AirplaneState.departingHigh, AirplaneState.arrivingHigh,
              AirplaneState.arrivingLow, AirplaneState.arrivingCloseFaf))
      .tryGetRandom()
      .ifPresent(q->q.getWriter().raiseEmergency());
  }

  public void unregisterPlane(Callsign callsign) {
    planes.remove(q -> q.getReader().getCallsign().equals(callsign));
    publicPlanes.remove(q -> q.getCallsign().equals(callsign));
  }

  public void updatePlanes() {
    for (Airplane plane : planes) {
      try {
        plane.elapseSecond();
      } catch (Exception ex) {
        throw new ApplicationException("Error processing elapseSecond() on plane " + plane.getReader().getCallsign() + ".", ex);
      }
    }
  }

//TODEL
//
//  private boolean isInSeparationConflictWithTraffic(ArrivalAirplaneTemplate template) {
//    Integer checkedAtEntryPointSeconds = null;
//
//    boolean ret = false;
//
//    for (Airplane plane : this.planes) {
//      IAirplane rdr = plane.getReader();
//      if (rdr.isDeparture())
//        continue;
//      if (rdr.getAtc().getTunedAtc().getType() != AtcType.ctr)
//        continue;
//
//      if (template.getEntryPoint().getNavaid().equals(rdr.getRouting().getEntryExitPoint()) == false)
//        continue;
//
//      double dist = Coordinates.getDistanceInNM(
//          rdr.getRouting().getEntryExitPoint().getCoordinate(), rdr.getCoordinate());
//      int atEntryPointSeconds = (int) (dist / rdr.getSha().getSpeed() * 3600);
//
//      if (checkedAtEntryPointSeconds == null) {
//        dist = Coordinates.getDistanceInNM(
//            template.getEntryPoint().getNavaid().getCoordinate(), template.getCoordinate());
//        checkedAtEntryPointSeconds = (int) (dist / template.getSpeed() * 3600);
//      }
//
//      if (Math.abs(atEntryPointSeconds - checkedAtEntryPointSeconds) < 120) {
//        ret = true;
//        break;
//      }
//    }
//    return ret;
//  }
}
