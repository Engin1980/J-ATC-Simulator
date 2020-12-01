package eng.jAtcSim.newLib.airplanes.pilots;

import eng.eSystem.geo.Coordinates;
import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.airplanes.contextLocal.Context;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;

public class ArrivalPilot extends BasicPilot {

  private final static double LOW_SPEED_DOWN_ALTITUDE = 11000;
  private final static double FAF_SPEED_DOWN_DISTANCE_IN_NM = 15;

  public ArrivalPilot(Airplane plane) {
    super(plane);
  }

  @Override
  protected void elapseSecondInternalBasic() {
    switch (rdr.getState()) {
      case arrivingHigh:
        if (rdr.getSha().getAltitude() < LOW_SPEED_DOWN_ALTITUDE)
          wrt.setState(AirplaneState.arrivingLow);
        else {
          setArrivingCloseFafStateIfReady();
        }
        break;
      case arrivingLow:
        // TODO this will not work for runways with FAF above FL100
        setArrivingCloseFafStateIfReady();
        break;
      case arrivingCloseFaf:
        break;
      default:
        super.throwIllegalStateException();
    }
  }

  @Override
  protected AirplaneState[] getInitialStates() {
    return new AirplaneState[]{
            AirplaneState.arrivingHigh,
            AirplaneState.arrivingLow,
            AirplaneState.arrivingCloseFaf,
            AirplaneState.flyingIaf2Faf
    };
  }

  @Override
  protected AirplaneState[] getValidStates() {
    return new AirplaneState[]{
            AirplaneState.arrivingHigh,
            AirplaneState.arrivingLow,
            AirplaneState.arrivingCloseFaf,
            AirplaneState.flyingIaf2Faf
    };
  }

  private void setArrivingCloseFafStateIfReady() {
    double distToFaf = Context.getArea().getCurrentRunwayConfiguration()
            .getArrivals().where(q -> q.isForCategory(rdr.getType().category))
            .minDouble(q -> Coordinates.getDistanceInNM(rdr.getCoordinate(), q.getThreshold().getEstimatedFafPoint()));
    if (distToFaf < FAF_SPEED_DOWN_DISTANCE_IN_NM) {
      wrt.setState(AirplaneState.arrivingCloseFaf);
    }
  }
}
