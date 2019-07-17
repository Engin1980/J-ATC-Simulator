package eng.jAtcSim.lib.airplanes.pilots.behaviors;

import eng.eSystem.geo.Coordinates;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.pilots.interfaces.forPilot.IPilotWriteSimple;

public class ArrivalBehavior extends BasicBehavior {

  private final static double LOW_SPEED_DOWN_ALTITUDE = 11000;
  private final static double FAF_SPEED_DOWN_DISTANCE_IN_NM = 15;

  @Override
  public String toLogString() {
    return "ARR";
  }

  @Override
  void _fly(IPilotWriteSimple pilot) {
    switch (pilot.getPlane().getState()) {
      case arrivingHigh:
        if (pilot.getPlane().getSha().getAltitude() < LOW_SPEED_DOWN_ALTITUDE)
          pilot.setBehaviorAndState(this, Airplane.State.arrivingLow);
        else {
          setArrivingCloseFafStateIfReady(pilot);
        }
        break;
      case arrivingLow:
        // TODO this will not work for runways with FAF above FL100
        setArrivingCloseFafStateIfReady(pilot);
        break;
      case arrivingCloseFaf:
        break;
      default:
        super.throwIllegalStateException(pilot);
    }
  }

  private void setArrivingCloseFafStateIfReady(IPilotWriteSimple pilot) {
    double distToFaf = Acc.atcTwr().getRunwayConfigurationInUse()
        .getArrivals().where(q -> q.isForCategory(pilot.getPlane().getType().category))
        .minDouble(q -> Coordinates.getDistanceInNM(pilot.getPlane().getCoordinate(), q.getThreshold().getEstimatedFafPoint()));
    if (distToFaf < FAF_SPEED_DOWN_DISTANCE_IN_NM) {
      pilot.setBehaviorAndState( this, Airplane.State.arrivingCloseFaf);
    }
  }

}
