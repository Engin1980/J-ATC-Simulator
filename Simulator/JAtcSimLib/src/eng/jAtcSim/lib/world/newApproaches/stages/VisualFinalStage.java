//package eng.jAtcSim.lib.world.newApproaches.stages;
//
//import eng.eSystem.collections.EMap;
//import eng.eSystem.collections.IMap;
//import eng.eSystem.geo.Coordinate;
//import eng.eSystem.geo.Coordinates;
//import eng.jAtcSim.lib.airplanes.interfaces.IPilot4Behavior;
//import eng.jAtcSim.lib.world.ActiveRunwayThreshold;
//
///**
// * Represents visual approach aimed to the runway threshold.
// * <p>
// * The point is that the airplane selects the specified point PX:
// * <ul>
// * <li>If the airplane is less than 2nm to the threshold, that point is in the middle distance
// * between the plane an the runway</li>
// * <li>If the airplane is more than 2nm to the threshold, that point is randomly between 2 and 5 nm
// * from the runway threshold</li>
// * </ul>
// * a) if the aip
// */
//public class VisualFinalStage implements IApproachStage {
//  private static final double MINIMAL_AIM_POINT_DISTANCE = .4;
//  private static final double MINIMAL_NORMAL_AIM_POINT_DISTANCE = 2;
//  private static final double MAXIMAL_NORMAL_AIM_POINT_DISTANCE = 5;
//  private static final double AIP_POINT_DELETE_DISTANCE = 1;
//  private static final double DEFAULT_SLOPE = 1;
//  private static IMap<String, AimInfo> aimPoints = new EMap<>();
//  private ActiveRunwayThreshold threshold;
//
//  public static class AimInfo{
//    public final Coordinate aimPoint;
//    public final double minimalRadius;
//
//    public AimInfo(Coordinate aimPoint, double minimalRadius) {
//      this.aimPoint = aimPoint;
//      this.minimalRadius = minimalRadius;
//    }
//  }
//
//  public VisualFinalStage(ActiveRunwayThreshold threshold) {
//    this.threshold = threshold;
//  }
//
//  @Override
//  public eResult initStage(IPilot4Behavior pilot) {
//
//    double distanceFromThreshold = Coordinates.getDistanceInNM(threshold.getCoordinate(), pilot.getCoordinate());
//    Coordinate aimPoint;
//    if (distanceFromThreshold < 2)
//      aimPoint = getAimPoint(pilot, true);
//    else
//      aimPoint = getAimPoint(pilot, false);
//
//    // distanceToAim is a distance of two minutes circle (standart turn 3°/deg)
//    // converted from circle to radius
//    double distanceToAim = (pilot.getAirplaneType().vApp * 2 / 60) / 2 / Math.PI;
//
//    if (distanceToAim) tady to nějak dopsat že může GA když je blbě moc
//
//        // vzdálenost bodu od přímky: https://matematika.cz/vzdalenost-bod-primka
//
//    aimPoints.set(pilot.getCallsign().toString(), new AimInfo(aimPoint, distanceToAim));
//
//
//
//
//  }
//
//  @Override
//  public void flyStage(IPilot4Behavior pilot) {
//    if (aimPoint != null) {
//      doFlyHeading(aimPoint, pilot);
//      deleteAimPointIfClose(pilot);
//    } else
//      doFlyHeading(this.threshold.getCoordinate(), pilot);
//
//    doFlyAltitude(pilot);
//  }
//
//  @Override
//  public eResult disposeStage(IPilot4Behavior pilot) {
//    return eResult.ok;
//  }
//
//  @Override
//  public boolean isFinishedStage(IPilot4Behavior pilot) {
//    return false;
//  }
//
//  private void doFlyAltitude(IPilot4Behavior pilot) {
//    double alt = ApproachStages.getTargetAltitudeBySlope(pilot.getCoordinate(), pilot.getAltitude(),
//        DEFAULT_SLOPE, this.threshold.getCoordinate(), this.threshold.getParent().getParent().getAltitude());
//    pilot.setAltitudeOrders(alt);
//  }
//
//  private void deleteAimPointIfClose(IPilot4Behavior pilot) {
//    double dst = Coordinates.getDistanceInNM(pilot.getCoordinate(), aimPoint);
//    if (dst < AIP_POINT_DELETE_DISTANCE)
//      this.aimPoint = null;
//  }
//
//  private void doFlyHeading(Coordinate point, IPilot4Behavior pilot) {
//    double hdg = Coordinates.getBearing(pilot.getCoordinate(), point);
//    pilot.setTargetHeading(hdg);
//  }
//
//  private Coordinate getAimPoint(IPilot4Behavior pilot, boolean aimAtThreshod) {
//    Coordinate ret;
//    if (aimAtThreshod)
//      ret = threshold.getCoordinate();
//    else {
//      int speed = pilot.getAirplaneType().vApp;
//      int verticalSpeed = speed * 5;
//      double flightTimeSeconds = 500 / (double) verticalSpeed;
//      double flightDistance = speed * flightTimeSeconds / 3600;
//      ret = Coordinates.getCoordinate(threshold.getCoordinate(), threshold.getOtherThreshold().getCourse(), flightDistance);
//    }
//    return ret;
//  }
//
//}
