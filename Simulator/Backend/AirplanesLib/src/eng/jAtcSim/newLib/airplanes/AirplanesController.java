package eng.jAtcSim.newLib.airplanes;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.ToDoException;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.context.AirplaneAcc;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;
import eng.jAtcSim.newLib.airplanes.templates.AirplaneTemplate;
import eng.jAtcSim.newLib.airplanes.templates.ArrivalAirplaneTemplate;
import eng.jAtcSim.newLib.airplanes.templates.DepartureAirplaneTemplate;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.Squawk;
import eng.jAtcSim.newLib.shared.enums.AtcType;

public class AirplanesController {
  private final IList<AirplaneTemplate> preparedPlanes = new EList<>();
  private final AirplaneList<Airplane> planes = new AirplaneList<>(
      q -> q.getReader().getCallsign(),
      q -> q.getReader().getSqwk()
  );
  private final AirplaneList<IAirplane> publicPlanes = new AirplaneList<>(q -> q.getCallsign(),
      q -> q.getSqwk());

  public void addNewPreparedPlanes(IList<AirplaneTemplate> airplaneTemplates) {
    preparedPlanes.add(airplaneTemplates);
    startNewPreparedPlanes();
  }

  public void elapseSecond() {
    for (Airplane plane : planes) {
      try {
        plane.elapseSecond();
      } catch (Exception ex) {
        throw new EApplicationException("Error processing elapseSecond() on plane " + plane.getReader().getCallsign() + ".", ex);
      }
    }
  }

  public void init() {
    AirplaneAcc.setAirplaneListProducer(() -> this.publicPlanes);
    AirplaneAcc.setAirplanesControllerProducer(() -> this);
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

    //TODO Implement this:
    throw new ToDoException();
  }

  private void convertAndRegisterPlane(AirplaneTemplate at) {
    EAssert.Argument.isNotNull(at, "at");
    Squawk sqwk = generateAvailableSquawk();
    Airplane airplane;
    if (at instanceof DepartureAirplaneTemplate) {
      airplane = Airplane.createDeparture((DepartureAirplaneTemplate) at, sqwk);
    } else if (at instanceof ArrivalAirplaneTemplate) {
      airplane = Airplane.createArrival((ArrivalAirplaneTemplate) at, sqwk);
    } else
      throw new EApplicationException("Unknown airplane template type " + at.getClass().getName());

    planes.add(airplane);
  }

  private Squawk generateAvailableSquawk() {
    IList<Squawk> squawks = this.planes.select(q -> q.getReader().getSqwk());
    Squawk ret;
    do {
      ret = Squawk.generate();
      if (squawks.contains(ret)) ret = null;
    } while (ret == null);
    return ret;
  }

  private boolean isInSeparationConflictWithTraffic(ArrivalAirplaneTemplate template) {
    Integer checkedAtEntryPointSeconds = null;

    boolean ret = false;

    for (Airplane plane : this.planes) {
      IAirplane rdr = plane.getReader();
      if (rdr.isDeparture())
        continue;
      if (rdr.getAtc().getTunedAtc().getType() != AtcType.ctr)
        continue;

      if (template.getEntryPoint().getNavaid().equals(rdr.getRouting().getEntryExitPoint()) == false)
        continue;

      double dist = Coordinates.getDistanceInNM(
          rdr.getRouting().getEntryExitPoint().getCoordinate(), rdr.getCoordinate());
      int atEntryPointSeconds = (int) (dist / rdr.getSha().getSpeed() * 3600);

      if (checkedAtEntryPointSeconds == null) {
        dist = Coordinates.getDistanceInNM(
            template.getEntryPoint().getNavaid().getCoordinate(), template.getCoordinate());
        checkedAtEntryPointSeconds = (int) (dist / template.getSpeed() * 3600);
      }

      if (Math.abs(atEntryPointSeconds - checkedAtEntryPointSeconds) < 120) {
        ret = true;
        break;
      }
    }
    return ret;
  }

  private void startNewPreparedPlanes() {
    int index = 0;
    while (index < preparedPlanes.count()) {
      AirplaneTemplate at = preparedPlanes.get(index);
      if (at instanceof ArrivalAirplaneTemplate && isInSeparationConflictWithTraffic((ArrivalAirplaneTemplate) at))
        index++;
      else {
        convertAndRegisterPlane(at);
        preparedPlanes.removeAt(index);
      }
    }
  }
}
