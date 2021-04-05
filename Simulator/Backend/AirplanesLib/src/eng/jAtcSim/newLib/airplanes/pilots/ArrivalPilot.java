package eng.jAtcSim.newLib.airplanes.pilots;

import eng.eSystem.Tuple;
import eng.eSystem.collections.IList;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.geo.Headings;
import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.airplanes.contextLocal.Context;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;
import eng.jAtcSim.newLib.area.RunwayConfiguration;
import exml.annotations.XConstructor;
import exml.annotations.XIgnored;
import exml.loading.XLoadContext;

public class ArrivalPilot extends BasicPilot {

  private final static double LOW_SPEED_DOWN_ALTITUDE = 11000;
  private final static double FAF_SPEED_DOWN_DISTANCE_IN_NM = 15;
  private static final double DEFAULT_ESTIMATED_FAF_DISTANCE = 8.5;
  @XIgnored
  private Tuple<RunwayConfiguration, IList<Coordinate>> estimatedThresholdFafPoints = null;

  public ArrivalPilot(Airplane plane) {
    super(plane);
  }

  @XConstructor
  private ArrivalPilot(XLoadContext ctx) {
    super(ctx);
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
            AirplaneState.arrivingLow
//            AirplaneState.arrivingCloseFaf
//            AirplaneState.flyingIaf2Faf // todo is this really required? plane should not enter arrival pilot with this state, or should?
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
    if (estimatedThresholdFafPoints == null || Context.getArea().getCurrentRunwayConfiguration() != estimatedThresholdFafPoints.getA())
      resetEstimatedThresholdFafPoints();

    double distToFaf = estimatedThresholdFafPoints.getB()
            .minDouble(q -> Coordinates.getDistanceInNM(rdr.getCoordinate(), q)).orElseThrow();
    if (distToFaf < FAF_SPEED_DOWN_DISTANCE_IN_NM) {
      wrt.setState(AirplaneState.arrivingCloseFaf);
    }
  }

  private void resetEstimatedThresholdFafPoints() {
    this.estimatedThresholdFafPoints = new Tuple<>(
            Context.getArea().getCurrentRunwayConfiguration(),
            Context.getArea()
                    .getCurrentRunwayConfiguration()
                    .getArrivals()
                    .where(q -> q.isForCategory(rdr.getType().category))
                    .select(q ->
                            Coordinates.getCoordinate(
                                    q.getThreshold().getCoordinate(),
                                    Headings.getOpposite(q.getThreshold().getCourse()),
                                    DEFAULT_ESTIMATED_FAF_DISTANCE)));
  }
}
