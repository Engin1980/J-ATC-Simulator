package eng.jAtcSim.newLib.airplanes.pilots;

import eng.eSystem.geo.Coordinates;
import eng.jAtcSim.newLib.airplanes.Airplane;
import eng.jAtcSim.newLib.airplanes.LocalInstanceProvider;

public class ArrivalPilot extends BasicPilot {

  private final static double LOW_SPEED_DOWN_ALTITUDE = 11000;
  private final static double FAF_SPEED_DOWN_DISTANCE_IN_NM = 15;

  public ArrivalPilot(IPilotsPlane plane) {
    super(plane);
  }

  @Override
  protected void elapseSecondInternal() {
    switch (plane.getState()) {
      case arrivingHigh:
        if (plane.getAltitude() < LOW_SPEED_DOWN_ALTITUDE)
          plane.setState(Airplane.State.arrivingLow);
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

  private void setArrivingCloseFafStateIfReady() {
    double distToFaf = LocalInstanceProvider.getCurrentRunwayConfiguration()
        .getArrivals().where(q -> q.isForCategory(plane.getType().category))
        .minDouble(q -> Coordinates.getDistanceInNM(plane.getCoordinate(), q.getThreshold().getEstimatedFafPoint()));
    if (distToFaf < FAF_SPEED_DOWN_DISTANCE_IN_NM) {
      plane.setState(Airplane.State.arrivingCloseFaf);
    }
  }
}
