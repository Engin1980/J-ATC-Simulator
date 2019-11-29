/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.area;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.jAtcSim.lib.airplanes.AirplaneType;
import eng.jAtcSim.lib.area.approaches.Approach;
import eng.jAtcSim.lib.area.xml.XmlLoader;
import eng.jAtcSim.lib.global.Headings;
import eng.jAtcSim.lib.global.PlaneCategoryDefinitions;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ChangeAltitudeCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ChangeHeadingCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ProceedDirectCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ThenCommand;
import eng.jAtcSim.lib.world.approaches.ApproachEntry;
import eng.jAtcSim.lib.world.approaches.GaRoute;
import eng.jAtcSim.lib.world.approaches.IafRoute;
import eng.jAtcSim.lib.world.approaches.stages.RadialWithDescendStage;
import eng.jAtcSim.lib.world.approaches.stages.RouteStage;

/**
 * @author Marek
 */
public class ActiveRunwayThreshold extends Parentable<ActiveRunway> {

  public static IList<ActiveRunwayThreshold> loadList(IReadOnlyList<XElement> sources, int airportAltitude,
                                                      NavaidList navaids,
                                                      IReadOnlyList<DARoute> routes, IReadOnlyList<IafRoute> iafRoutes,
                                                      IReadOnlyList<GaRoute> gaRoutes) {
    assert sources.size() == 2 : "There must be two thresholds";

    Coordinate aCoordinate = XmlLoader.loadCoordinate(sources.get(0), "coordinate");
    Coordinate bCoordinate = XmlLoader.loadCoordinate(sources.get(1), "coordinate");

    ActiveRunwayThreshold a = ActiveRunwayThreshold.load(sources.get(0), airportAltitude, navaids,
        bCoordinate, routes, iafRoutes, gaRoutes);
    ActiveRunwayThreshold b = ActiveRunwayThreshold.load(sources.get(1), airportAltitude, navaids,
        aCoordinate, routes, iafRoutes, gaRoutes);
    a.other = b;
    b.other = a;

    IList<ActiveRunwayThreshold> ret = new EList<>();
    ret.add(a);
    ret.add(b);
    return ret;
  }

  private static ActiveRunwayThreshold load(XElement source, int airportAltitude, NavaidList navaids,
                                            Coordinate otherThresholdCoordinate,
                                            IReadOnlyList<DARoute> routes, IReadOnlyList<IafRoute> iafRoutes,
                                            IReadOnlyList<GaRoute> gaRoutes) {
    XmlLoader.setContext(source);
    String name = XmlLoader.loadString("name");
    Coordinate coordinate = XmlLoader.loadCoordinate("coordinate");
    int initialDepartureAltitude = XmlLoader.loadInteger("initialDepartureAltitude");
    String mappingString = XmlLoader.loadString("mapping");
    IList<String> mapping = new EList<>(mappingString.split(";"));

    double course = Coordinates.getBearing(coordinate, otherThresholdCoordinate);
    Coordinate estimatedFafPoint = Coordinates.getCoordinate(coordinate, Headings.getOpposite(course), 9);

    IList<DARoute> thresholdRoutes = routes.where(q -> q.isMappingMatch(mapping));
    IList<Approach> approaches = Approach.loadList(source.getChild("approaches").getChildren(),
        coordinate, (int) Math.round(course), airportAltitude, navaids, iafRoutes, gaRoutes);

    if (approaches.isNone(q -> q.getType() == Approach.ApproachType.visual)){
      Approach visual = Approach.generateDefaultVisualApproach(name,
          coordinate, course, airportAltitude, navaids);
      approaches.add(visual);
    }

    ActiveRunwayThreshold ret = new ActiveRunwayThreshold(
        name, coordinate, course, initialDepartureAltitude, estimatedFafPoint, approaches, thresholdRoutes);
    return ret;
  }

  private final IList<Approach> approaches;
  private final IList<DARoute> routes;
  private final String name;
  private final Coordinate coordinate;
  private final double course;
  private final int initialDepartureAltitude;
  @Deprecated
  private final Coordinate estimatedFafPoint;
  private ActiveRunwayThreshold other;

  private ActiveRunwayThreshold(String name, Coordinate coordinate, double course,
                                int initialDepartureAltitude, Coordinate estimatedFafPoint,
                                IList<Approach> approaches, IList<DARoute> routes) {
    this.approaches = approaches;
    this.routes = routes;
    this.name = name;
    this.coordinate = coordinate;
    this.initialDepartureAltitude = initialDepartureAltitude;
    this.course = course;
    this.estimatedFafPoint = estimatedFafPoint;
  }

  public IList<Approach> getApproaches() {
    return approaches;
  }

  public IReadOnlyList<Approach> getApproaches(Approach.ApproachType type, char category) {
    IList<Approach> ret = this.approaches.where(q -> q.getType() == type
        && q.getEntries().isAny(p -> p.isForCategory(category)));
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

  public Coordinate getCoordinate() {
    return coordinate;
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

  public IList<DARoute> getRoutes() {
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
