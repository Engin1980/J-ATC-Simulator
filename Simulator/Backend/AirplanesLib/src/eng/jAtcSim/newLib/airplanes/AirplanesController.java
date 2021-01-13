package eng.jAtcSim.newLib.airplanes;

import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
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
import exml.XContext;
import exml.annotations.XIgnored;

public class AirplanesController implements IXPersistable {

  @XIgnored private final AirplaneList planes = new AirplaneList();
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

    for (Airplane plane : planes) {
      this.publicPlanes.add(plane.getReader());

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
      throw new EApplicationException("Unknown airplane template type " + at.getClass().getName());

    this.planes.add(airplane);
    this.publicPlanes.add(airplane.getReader());

    return airplane.getReader();
  }

  @Override
  public void save(XElement elm, XContext ctx) {
    ctx.saver.saveFieldItems(this, "planes", Airplane.class, elm);
  }

  @Override
  public void load(XElement elm, XContext ctx) {
//    ctx.loader.loadField(this, "planes", elm);
  }

  public void throwEmergency() {
    Airplane p = this.planes
            .where(q -> q.getReader().getState().is(AirplaneState.departingLow,
                    AirplaneState.departingHigh, AirplaneState.arrivingHigh,
                    AirplaneState.arrivingLow, AirplaneState.arrivingCloseFaf))
            .tryGetRandom();
    if (p != null)
      p.getWriter().raiseEmergency();
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
        throw new EApplicationException("Error processing elapseSecond() on plane " + plane.getReader().getCallsign() + ".", ex);
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
