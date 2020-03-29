/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.newLib.area;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.geo.Headings;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.area.approaches.Approach;
import eng.jAtcSim.newLib.area.routes.DARoute;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;

import static eng.eSystem.utilites.FunctionShortcuts.*;

/**
 * @author Marek
 */
public class ActiveRunwayThreshold extends Parentable<ActiveRunway> {

  public static class Prototype {
    public String name;
    public Coordinate coordinate;
    public int initialDepartureAltitude;
    public IList<Approach> approaches;
    public IList<DARoute> routes;

    public Prototype(String name, Coordinate coordinate, int initialDepartureAltitude, IList<Approach> approaches, IList<DARoute> routes) {
      this.name = name;
      this.coordinate = coordinate;
      this.initialDepartureAltitude = initialDepartureAltitude;
      this.approaches = approaches;
      this.routes = routes;
    }
  }

  public static IList<ActiveRunwayThreshold> create(
      Prototype firstThreshold,
      Prototype secondThreshold) {
    EAssert.Argument.isNotNull(firstThreshold, "Parameter 'firstThreshold' cannot be null.");
    EAssert.Argument.isNotNull(secondThreshold, "Parameter 'secondThreshold' cannot be null.");

    ActiveRunwayThreshold a = new ActiveRunwayThreshold(
        firstThreshold.name, firstThreshold.coordinate, firstThreshold.initialDepartureAltitude,
        firstThreshold.approaches, firstThreshold.routes);
    ActiveRunwayThreshold b = new ActiveRunwayThreshold(
        secondThreshold.name, secondThreshold.coordinate, secondThreshold.initialDepartureAltitude,
        secondThreshold.approaches, secondThreshold.routes);

    a.other = b;
    b.other = a;
    a.course = Coordinates.getBearing(a.coordinate, b.coordinate);
    b.course = Coordinates.getBearing(b.coordinate, a.coordinate);

    IList<ActiveRunwayThreshold> ret = new EList<>();
    ret.add(a);
    ret.add(b);
    return ret;
  }

  private final IList<Approach> approaches;
  private final IReadOnlyList<DARoute> routes;
  private final String name;
  private final Coordinate coordinate;
  private final int initialDepartureAltitude;
  private double course;
  private Coordinate estimatedFafPoint;
  private ActiveRunwayThreshold other;

  public ActiveRunwayThreshold(String name, Coordinate coordinate, int initialDepartureAltitude,
                               IList<Approach> approaches, IReadOnlyList<DARoute> routes) {
    EAssert.Argument.isNotNull(approaches, "approaches");
    EAssert.Argument.isNotNull(routes, "routes");
    EAssert.Argument.isNotNull(coordinate, "coordinate");
    EAssert.Argument.matchPattern(name, "^\\d{2}[LRC]?$", sf("Runway name '%s' is not valid.", name));
    EAssert.Argument.isTrue(initialDepartureAltitude > 0);
    this.approaches = approaches;
    this.routes = routes;
    this.name = name;
    this.coordinate = coordinate;
    this.initialDepartureAltitude = initialDepartureAltitude;

    // add visual approach if any exists
    if (this.approaches.isNone(q -> q.getType() == Approach.ApproachType.visual)) {
      Approach visual = Approach.generateDefaultVisualApproach(this);
      this.approaches.add(visual);
    }

    // estimate faf
    this.estimatedFafPoint = Coordinates.getCoordinate(
        this.coordinate,
        Headings.getOpposite(this.course),
        9);
  }

  public IReadOnlyList<Approach> getApproaches() {
    return approaches;
  }

  public IReadOnlyList<Approach> getApproaches(Approach.ApproachType type, char category) {
    IList<Approach> ret = this.approaches.where(q -> q.getType() == type
        && q.getEntries().isAny(p -> p.isForCategory(category)));
    return ret;
  }

//  public DARoute getArrivalRouteForPlane(AirplaneType type, int currentAltitude, Navaid mainNavaid, boolean canBeVectoring) {
//    DARoute ret = this.getRoutes().where(
//        q -> q.getType() == DARoute.eType.transition
//            && q.isValidForCategory(type.category)
//            && q.getMaxMrvaAltitude() < currentAltitude
//            && q.getMainNavaid().equals(mainNavaid))
//        .tryGetRandom();
//    if (ret == null)
//      ret = this.getRoutes().where(
//          q -> q.getType() == DARoute.eType.star
//              && q.isValidForCategory(type.category)
//              && q.getMaxMrvaAltitude() < currentAltitude
//              && q.getMainNavaid().equals(mainNavaid))
//          .tryGetRandom();
//    if (ret == null && canBeVectoring)
//      ret = DARoute.createNewVectoringByFix(mainNavaid);
//    return ret;
//  }

  public Coordinate getCoordinate() {
    return coordinate;
  }

  public double getCourse() {
    return this.course;
  }

  public int getCourseInt() {
    return (int) Math.round(this.course);
  }

//  public DARoute getDepartureRouteForPlane(AirplaneType type, Navaid mainNavaid, boolean canBeVectoring) {
//    DARoute ret = this.getRoutes().where(
//        q -> q.getType() == DARoute.eType.sid
//            && q.isValidForCategory(type.category)
//            && q.getMaxMrvaAltitude() < type.maxAltitude
//            && q.getMainNavaid().equals(mainNavaid))
//        .tryGetRandom();
//    if (ret == null && canBeVectoring)
//      ret = DARoute.createNewVectoringByFix(mainNavaid);
//    return ret;
//  }

  @Deprecated
  public Coordinate getEstimatedFafPoint() {
    return estimatedFafPoint;
  }

  public String getFullName() {
    return getParent().getParent().getIcao() + this.getName();
  }

  public int getInitialDepartureAltitude() {
    return initialDepartureAltitude;
  }

  public String getName() {
    return name;
  }

  public ActiveRunwayThreshold getOtherThreshold() {
    return other;
  }

  public IReadOnlyList<ActiveRunwayThreshold> getParallelGroup() {
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

  public IReadOnlyList<DARoute> getRoutes() {
    return routes;
  }

  @Override
  public String toString() {
    return this.getName() + "{rwyThr}";
  }

  public Approach tryGetHighestApproachExceptVisuals() {
    Approach ret;

    ret = this.approaches.tryGetFirst(q -> q.getType() == Approach.ApproachType.ils_III);
    if (ret == null)
      ret = this.approaches.tryGetFirst(q -> q.getType() == Approach.ApproachType.ils_II);
    if (ret == null)
      ret = this.approaches.tryGetFirst(q -> q.getType() == Approach.ApproachType.ils_I);
    if (ret == null)
      ret = this.approaches.tryGetFirst(q -> q.getType() == Approach.ApproachType.gnss);
    if (ret == null)
      ret = this.approaches.tryGetFirst(q -> q.getType() == Approach.ApproachType.vor);
    if (ret == null)
      ret = this.approaches.tryGetFirst(q -> q.getType() == Approach.ApproachType.ndb);

    return ret;
  }


}
