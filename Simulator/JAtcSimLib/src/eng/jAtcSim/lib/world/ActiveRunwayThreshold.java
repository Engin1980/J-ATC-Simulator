/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.world;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.jAtcSim.lib.airplanes.AirplaneType;
import eng.jAtcSim.lib.global.Headings;
import eng.jAtcSim.lib.world.approaches.Approach;
import eng.jAtcSim.lib.world.approaches.IafRoute;
import eng.jAtcSim.lib.world.xml.XmlLoader;

/**
 * @author Marek
 */
public class ActiveRunwayThreshold extends Parentable<Runway> {

  public static IList<ActiveRunwayThreshold> loadList(IReadOnlyList<XElement> sources, IList<DARoute> routes, IList<IafRoute> iafRoutes){
    assert sources.size() == 2 : "There must be two thresholds";

    ActiveRunwayThreshold a = ActiveRunwayThreshold.load(sources.get(0), routes, iafRoutes);
    ActiveRunwayThreshold b = ActiveRunwayThreshold.load(sources.get(1), routes, iafRoutes);
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

    IList<ActiveRunwayThreshold> ret = new EList<>();
    ret.add(a);
    ret.add(b);
    return ret;
  }

  private static ActiveRunwayThreshold load(XElement source, IList<DARoute> routes, IList<IafRoute> iafRoutes){
    XmlLoader.setContext(source);
    String name = XmlLoader.loadString("name", true);
    Coordinate coordinate = XmlLoader.loadCoordinate("coordinate",true);
    int initialDepartureAltitude = XmlLoader.loadInteger("initialDepartureAltitude", true);
    String mappingString = XmlLoader.loadString("mapping", true);
    IList<String> mapping = new EList<>(mappingString.split(";"));

    IList<DARoute> thresholdRoutes = routes.where(q->q.mappingAccepts(mapping));
    IList<Approach> approaches = Approach.loadList(source.getChild("approaches").getChildren(), iafRoutes);

    ActiveRunwayThreshold ret = new ActiveRunwayThreshold(
        name, coordinate, initialDepartureAltitude, approaches, thresholdRoutes);
    return ret;
  }

  private final IList<Approach> approaches;
  private final IList<DARoute> routes;
  private final String name;
  private final Coordinate coordinate;
  private double course;
  private final int initialDepartureAltitude;
  private ActiveRunwayThreshold other;
  @Deprecated
  private Coordinate estimatedFafPoint;

  private ActiveRunwayThreshold(String name, Coordinate coordinate, int initialDepartureAltitude, IList<Approach> approaches, IList<DARoute> routes) {
    this.approaches = approaches;
    this.routes = routes;
    this.name = name;
    this.coordinate = coordinate;
    this.initialDepartureAltitude = initialDepartureAltitude;
  }

  public int getInitialDepartureAltitude() {
    return initialDepartureAltitude;
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

  public IList<DARoute> getRoutes() {
    return routes;
  }

  public double getCourse() {
    return this.course;
  }

  public DARoute getDepartureRouteForPlane(AirplaneType type, Navaid mainNavaid, boolean canBeVectoring) {
    DARoute ret = this.getRoutes().where(
        q -> q.getType() == DARoute.eType.sid
            && q.isValidForCategory(type.category)
            && q.getMaxMrvaAltitude() < type.maxAltitude
            && q.getMainNavaid().equals(mainNavaid))
        .tryGetRandom();
    if (ret == null && canBeVectoring)
      ret = DARoute.createNewVectoringByFix(mainNavaid);
    return ret;
  }

  public DARoute getArrivalRouteForPlane(AirplaneType type, int currentAltitude, Navaid mainNavaid, boolean canBeVectoring) {
    DARoute ret = this.getRoutes().where(
        q -> q.getType() == DARoute.eType.transition
            && q.isValidForCategory(type.category)
            && q.getMaxMrvaAltitude() < currentAltitude
            && q.getMainNavaid().equals(mainNavaid))
        .tryGetRandom();
    if (ret == null)
      ret = this.getRoutes().where(
          q -> q.getType() == DARoute.eType.star
              && q.isValidForCategory(type.category)
              && q.getMaxMrvaAltitude() < currentAltitude
              && q.getMainNavaid().equals(mainNavaid))
          .tryGetRandom();
    if (ret == null && canBeVectoring)
      ret = DARoute.createNewVectoringByFix(mainNavaid);
    return ret;
  }

  public IReadOnlyList<Approach> getApproaches(Approach.ApproachType type, char category) {
    IList<Approach> ret = this.approaches.where(q -> q.getType() == type && q.getPlaneCategories().contains(category));
    return ret;
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

  public String getFullName() {
    return getParent().getParent().getIcao() + this.getName();
  }
}
