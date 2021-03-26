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
import eng.eSystem.exceptions.ToDoException;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.geo.Headings;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.area.approaches.Approach;
import eng.jAtcSim.newLib.area.routes.DARoute;
import eng.jAtcSim.newLib.shared.enums.ApproachType;
import exml.IXPersistable;
import exml.loading.XLoadContext;
import exml.saving.XSaveContext;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

/**
 * @author Marek
 */
public class ActiveRunwayThreshold extends Parentable<ActiveRunway> {

  public static class Prototype {
    public final String name;
    public final Coordinate coordinate;
    public final int initialDepartureAltitude;
    public final IList<Approach> approaches;
    public final IList<DARoute> routes;
    public final int accelerationAltitude;

    public Prototype(String name, Coordinate coordinate, int initialDepartureAltitude, int accelerationAltitude,
                     IList<Approach> approaches, IList<DARoute> routes) {
      this.name = name;
      this.coordinate = coordinate;
      this.initialDepartureAltitude = initialDepartureAltitude;
      this.approaches = approaches;
      this.routes = routes;
      this.accelerationAltitude = accelerationAltitude;
    }
  }

  public static IList<ActiveRunwayThreshold> create(
          Prototype firstThreshold,
          Prototype secondThreshold) {
    EAssert.Argument.isNotNull(firstThreshold, "Parameter 'firstThreshold' cannot be null.");
    EAssert.Argument.isNotNull(secondThreshold, "Parameter 'secondThreshold' cannot be null.");

    ActiveRunwayThreshold a = new ActiveRunwayThreshold(
            firstThreshold.name, firstThreshold.coordinate, firstThreshold.initialDepartureAltitude,
            firstThreshold.accelerationAltitude,
            firstThreshold.approaches, firstThreshold.routes);
    ActiveRunwayThreshold b = new ActiveRunwayThreshold(
            secondThreshold.name, secondThreshold.coordinate, secondThreshold.initialDepartureAltitude,
            secondThreshold.accelerationAltitude,
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
  private final int accelerationAltitude;
  private double course;
  private Coordinate estimatedFafPoint;
  private ActiveRunwayThreshold other;

  public ActiveRunwayThreshold(String name, Coordinate coordinate, int initialDepartureAltitude,
                               int accelerationAltitude,
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
    this.accelerationAltitude = accelerationAltitude;

    // estimate faf
    this.estimatedFafPoint = Coordinates.getCoordinate(
            this.coordinate,
            Headings.getOpposite(this.course),
            9);
  }

  public void bind() {
    this.approaches.forEach(q -> q.setParent(this));
  }

  public int getAccelerationAltitude() {
    return accelerationAltitude;
  }

  public IReadOnlyList<Approach> getApproaches() {
    return approaches;
  }

  public Coordinate getCoordinate() {
    return coordinate;
  }

  public double getCourse() {
    return this.course;
  }

  public int getCourseInt() {
    return (int) Math.round(this.course);
  }

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
    if (this.getParent() == null || this.getParent().getParent() == null)
      return "???? " + this.name;
    else
      return this.getParent().getParent().getIcao() + " " + this.name;
  }

  public Approach tryGetHighestApproachExceptVisuals() {
    Approach ret;

    ret = this.approaches.tryGetFirst(q -> q.getType() == ApproachType.ils_III);
    if (ret == null)
      ret = this.approaches.tryGetFirst(q -> q.getType() == ApproachType.ils_II);
    if (ret == null)
      ret = this.approaches.tryGetFirst(q -> q.getType() == ApproachType.ils_I);
    if (ret == null)
      ret = this.approaches.tryGetFirst(q -> q.getType() == ApproachType.gnss);
    if (ret == null)
      ret = this.approaches.tryGetFirst(q -> q.getType() == ApproachType.vor);
    if (ret == null)
      ret = this.approaches.tryGetFirst(q -> q.getType() == ApproachType.ndb);

    return ret;
  }
}
