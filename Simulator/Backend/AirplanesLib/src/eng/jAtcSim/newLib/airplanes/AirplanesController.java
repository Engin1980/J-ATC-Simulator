package eng.jAtcSim.newLib.airplanes;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.geo.Coordinates;
import eng.jAtcSim.newLib.Acc;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;
import eng.jAtcSim.newLib.airplanes.internal.InternalAcc;
import eng.jAtcSim.newLib.area.AreaAcc;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.exceptions.ToDoException;
import eng.jAtcSim.newLib.stats.StatsAcc;
import eng.jAtcSim.newLib.traffic.movementTemplating.MovementTemplate;

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

    planes.remove(q->q.getReader().getCallsign().equals(callsign));
    publicPlanes.remove(q->q.getCallsign().equals(callsign));

    //TODO Implement this:
    throw new ToDoException();
  }

  private void startNewPreparedPlanes() {
    int index = 0;
    while (index < preparedPlanes.count()) {
      AirplaneTemplate at = preparedPlanes.get(index);
      if (isInSeparationConflictWithTraffic(at))
        index++;
      else {
        convertAndRegisterPlane(at);
        preparedPlanes.removeAt(index);
      }
    }
  }

  private boolean isInSeparationConflictWithTraffic(AirplaneTemplate checkedPlane) {
    Integer checkedAtEntryPointSeconds = null;

    boolean ret = false;
    for (Airplane plane : this.planes) {
      if (plane.getReader().isDeparture())
        continue;
      tady pokračovat ale nevím jak.
      if (prm.getResponsibleAtc(plane) != ctrAtc)
        continue;
      if (checkedPlane.entryExitPoint.equals(plane.getRoutingModule().getEntryExitPoint()) == false)
        continue;

      double dist = Coordinates.getDistanceInNM(
          plane.getRoutingModule().getEntryExitPoint().getCoordinate(), plane.getCoordinate());
      int atEntryPointSeconds = (int) (dist / plane.getSha().getSpeed() * 3600);

      if (checkedAtEntryPointSeconds == null) {
        dist = Coordinates.getDistanceInNM(
            checkedPlane.getRoutingModule().getEntryExitPoint().getCoordinate(), checkedPlane.getCoordinate());
        checkedAtEntryPointSeconds = (int) (dist / checkedPlane.getSha().getSpeed() * 3600);
      }

      if (Math.abs(atEntryPointSeconds - checkedAtEntryPointSeconds) < 120) {
        ret = true;
        break;
      }
    }
    return ret;
  }
}
