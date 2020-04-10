//package eng.jAtcSim.newLib.airplanes.pilots.behaviors_old;
//
//import eng.eSystem.geo.Coordinates;
//import eng.jAtcSim.newLib.Acc;
//import eng.jAtcSim.newLib.area.airplanes.Airplane;
//import eng.jAtcSim.newLib.area.airplanes.interfaces.IAirplaneWriteSimple;
//
//public class ArrivalBehavior extends BasicBehavior {
//
//  private final static double LOW_SPEED_DOWN_ALTITUDE = 11000;
//  private final static double FAF_SPEED_DOWN_DISTANCE_IN_NM = 15;
//
//  @Override
//  public String toLogString() {
//    return "ARR";
//  }
//
//  @Override
//  void _fly(IAirplaneWriteSimple plane) {
//    switch (plane.getState()) {
//      case arrivingHigh:
//        if (plane.getSha().getAltitude() < LOW_SPEED_DOWN_ALTITUDE)
//          plane.setBehaviorAndState(this, Airplane.State.arrivingLow);
//        else {
//          setArrivingCloseFafStateIfReady(plane);
//        }
//        break;
//      case arrivingLow:
//        // TODO this will not work for runways with FAF above FL100
//        setArrivingCloseFafStateIfReady(plane);
//        break;
//      case arrivingCloseFaf:
//        break;
//      default:
//        super.throwIllegalStateException(plane);
//    }
//  }
//
//  private void setArrivingCloseFafStateIfReady(IAirplaneWriteSimple plane) {
//    double distToFaf = Acc.atcTwr().getRunwayConfigurationInUse()
//        .getArrivals().where(q -> q.isForCategory(plane.getType().category))
//        .minDouble(q -> Coordinates.getDistanceInNM(plane.getCoordinate(), q.getThreshold().getEstimatedFafPoint()));
//    if (distToFaf < FAF_SPEED_DOWN_DISTANCE_IN_NM) {
//      plane.setBehaviorAndState( this, Airplane.State.arrivingCloseFaf);
//    }
//  }
//
//}
