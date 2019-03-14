/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.world;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.utilites.CollectionUtils;
import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.lib.airplanes.AirplaneType;
import eng.jAtcSim.lib.airplanes.pilots.approachStages.ApproachInfo;
import eng.jAtcSim.lib.global.Headings;
import eng.jAtcSim.lib.world.approaches.*;

import java.util.List;

/**
 * @author Marek
 */
public class ActiveRunwayThreshold {

  public static ActiveRunwayThreshold[] create(
      String aName, Coordinate aCoordinate, int aInitialDepartureAltitude, IList<Approach> aApproaches, IList<Route> aRoutes,
      String bName, Coordinate bCoordinate, int bInitialDepartureAltitude, IList<Approach> bApproaches, IList<Route> bRoutes,
      ActiveRunway parent
  ) {
    ActiveRunwayThreshold a = new ActiveRunwayThreshold(aName, aCoordinate, aInitialDepartureAltitude, aApproaches, aRoutes, parent);
    ActiveRunwayThreshold b = new ActiveRunwayThreshold(bName, bCoordinate, bInitialDepartureAltitude, bApproaches, bRoutes, parent);

    a.other = b;
    b.other = a;
    a.course = Coordinates.getBearing(a.coordinate, b.coordinate);
    b.course = Coordinates.getBearing(b.coordinate, a.coordinate);
    a.estimatedFafPoint = Coordinates.getCoordinate(
        a.coordinate,
        Headings.getOpposite(a.course),
        9);
    b.estimatedFafPoint = Coordinates.getCoordinate(
        b.coordinate,
        Headings.getOpposite(b.course),
        9);

    return new ActiveRunwayThreshold[]{a, b};
  }

  private final IList<Approach> approaches;
  private final IList<Route> routes;
  private final String name;
  private final Coordinate coordinate;
  private final ActiveRunway parent;
  private double course;
  private final int initialDepartureAltitude;
  private ActiveRunwayThreshold other;
  private final boolean preferred = false;
  @Deprecated
  private Coordinate estimatedFafPoint;

  private ActiveRunwayThreshold(String name, Coordinate coordinate, int initialDepartureAltitude, IList<Approach> approaches, IList<Route> routes, ActiveRunway parent) {
    this.approaches = approaches;
    this.routes = routes;
    this.name = name;
    this.coordinate = coordinate;
    this.parent = parent;
    this.initialDepartureAltitude = initialDepartureAltitude;
  }

  public int getInitialDepartureAltitude() {
    return initialDepartureAltitude;
  }

  public boolean isPreferred() {
    return preferred;
  }

  public String getName() {
    return name;
  }

  public Coordinate getCoordinate() {
    return coordinate;
  }

  public IList<Approach> getApproaches() {
    return approaches;
  }

  public IList<Route> getRoutes() {
    return routes;
  }

  public ActiveRunway getParent() {
    return parent;
  }

  public double getCourse() {
    return this.course;
  }

  public Route getDepartureRouteForPlane(AirplaneType type, Navaid mainNavaid, boolean canBeVectoring) {
    Route ret = this.getRoutes().where(
        q -> q.getType() == Route.eType.sid
            && q.isValidForCategory(type.category)
            && q.getMaxMrvaAltitude() < type.maxAltitude
            && q.getMainNavaid().equals(mainNavaid))
        .tryGetRandom();
    if (ret == null && canBeVectoring)
      ret = Route.createNewVectoringByFix(mainNavaid);
    return ret;
  }

  public Route getArrivalRouteForPlane(AirplaneType type, int currentAltitude, Navaid mainNavaid, boolean canBeVectoring) {
    Route ret = this.getRoutes().where(
        q -> q.getType() == Route.eType.transition
            && q.isValidForCategory(type.category)
            && q.getMaxMrvaAltitude() < currentAltitude
            && q.getMainNavaid().equals(mainNavaid))
        .tryGetRandom();
    if (ret == null)
      ret = this.getRoutes().where(
          q -> q.getType() == Route.eType.star
              && q.isValidForCategory(type.category)
              && q.getMaxMrvaAltitude() < currentAltitude
              && q.getMainNavaid().equals(mainNavaid))
          .tryGetRandom();
    if (ret == null && canBeVectoring)
      ret = Route.createNewVectoringByFix(mainNavaid);
    return ret;
  }

  public ApproachInfo tryGetCurrentApproachInfo(Approach.ApproachType type, char category, Coordinate planePosition) {

    ApproachInfo ret;
    if (type == Approach.ApproachType.visual)
      ret = Approach.createVisualApproachInfo(this, planePosition);
    else
      ret =
          Approach.tryGetCurrentApproachInfo(this.approaches, category, type, planePosition);
    return ret;
  }

  public Approach getHighestApproach() {
    Approach ret;

    ret = CollectionUtils.tryGetFirst(this.approaches, o -> o instanceof IlsApproach);
    if (ret == null)
      ret = CollectionUtils.tryGetFirst(this.approaches, o -> o instanceof GnssApproach);
    if (ret == null)
      ret = CollectionUtils.tryGetFirst(this.approaches, o -> o instanceof UnpreciseApproach);
    //TODO original idea was that visual approaches will be generated and put into list of approaches
    //now it is not done such way, so this method returns null
//    if (ret == null)
//      ret = CollectionUtils.tryGetFirst(this.approaches, o -> o instanceof VisualApproach);

//    assert ret != null;

    return ret;
  }

  public IList<ActiveRunwayThreshold> getParallelGroup() {
    IList<ActiveRunwayThreshold> ret = new EList<>();

    double crs = this.getCourse();
    for (ActiveRunway runway : this.getParent().getParent().getRunways()) {
      for (ActiveRunwayThreshold threshold : runway.getThresholds()) {
        //TODO this may fail for
        if (Headings.isBetween(crs - 1, threshold.getCourse(), crs + 1)) {
          ret.add(threshold);
          break;
        }
      }
    }

    assert ret.contains(this) : "Parallel thresholds group should contains the invoking one.";

    return ret;
  }

  public ActiveRunwayThreshold getOtherThreshold() {
    return other;
  }

  @Deprecated
  public Coordinate getEstimatedFafPoint() {
    return estimatedFafPoint;
  }

  @Override
  public String toString() {
    return this.getName() + "{rwyThr}";
  }
}
