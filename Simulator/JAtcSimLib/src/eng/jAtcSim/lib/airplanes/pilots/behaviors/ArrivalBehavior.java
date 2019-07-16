package eng.jAtcSim.lib.airplanes.pilots.behaviors;

import eng.eSystem.geo.Coordinates;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.pilots.interfaces.forPilot.IPilot5Behavior;

public class ArrivalBehavior extends BasicBehavior {

  private final static double LOW_SPEED_DOWN_ALTITUDE = 11000;
  private final static double FAF_SPEED_DOWN_DISTANCE_IN_NM = 15;

  @Override
  void _fly(IPilot5Behavior pilot) {
    switch (pilot.getState()) {
      case arrivingHigh:
        if (pilot.getAltitude() < LOW_SPEED_DOWN_ALTITUDE)
          super.setBehaviorAndState(pilot, this, Airplane.State.arrivingLow);
        else {
          double distToFaf = Acc.atcTwr().getRunwayConfigurationInUse()
              .getArrivals().where(q -> q.isForCategory(pilot.getAirplaneType().category))
              .minDouble(q -> Coordinates.getDistanceInNM(pilot.getCoordinate(), q.getThreshold().getEstimatedFafPoint()));
          if (distToFaf < FAF_SPEED_DOWN_DISTANCE_IN_NM) {
            super.setBehaviorAndState(pilot, this, Airplane.State.arrivingCloseFaf);
          }
        }
        break;
      case arrivingLow:
        // TODO this will not work for runways with FAF above FL100
        double distToFaf = Acc.atcTwr().getRunwayConfigurationInUse()
            .getArrivals().where(q -> q.isForCategory(pilot.getAirplaneType().category))
            .minDouble(q -> Coordinates.getDistanceInNM(pilot.getCoordinate(), q.getThreshold().getEstimatedFafPoint()));
        if (distToFaf < FAF_SPEED_DOWN_DISTANCE_IN_NM) {
          super.setBehaviorAndState(pilot, this, Airplane.State.arrivingCloseFaf);
        }
        break;
      case arrivingCloseFaf:
        break;
      default:
        super.throwIllegalStateException(pilot);
    }

    if (!pilot.isEmergency())
      super.processDivertManagement(pilot);
  }


  @Override
  public String toLogString() {
    return "ARR";
  }

}
