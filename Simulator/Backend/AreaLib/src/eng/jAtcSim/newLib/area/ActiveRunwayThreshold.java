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
import eng.jAtcSim.newLib.area.approaches.Approach;
import eng.jAtcSim.newLib.area.routes.DARoute;
import eng.jAtcSim.newLib.shared.xml.XmlLoader;

/**
 * @author Marek
 */
public class ActiveRunwayThreshold extends Parentable<ActiveRunway> {

  public static IList<ActiveRunwayThreshold> loadBoth(IReadOnlyList<XElement> sources, ActiveRunway runway) {
    assert sources.size() == 2 : "There must be two thresholds";

    ActiveRunwayThreshold a = ActiveRunwayThreshold.load(sources.get(0), runway);
    ActiveRunwayThreshold b = ActiveRunwayThreshold.load(sources.get(1), runway);
    bindOppositeThresholds(a, b);

    IList<ActiveRunwayThreshold> ret = new EList<>();
    ret.add(a);
    ret.add(b);
    return ret;
  }

  private static void bindOppositeThresholds(ActiveRunwayThreshold a, ActiveRunwayThreshold b) {
    a.other = b;
    b.other = a;

    a.course = Coordinates.getBearing(a.coordinate, b.coordinate);
    b.course = Coordinates.getBearing(b.coordinate, a.coordinate);

    a.estimatedFafPoint = Coordinates.getCoordinate(a.coordinate,
        Headings.getOpposite(a.course), 9);
    b.estimatedFafPoint = Coordinates.getCoordinate(b.coordinate,
        Headings.getOpposite(b.course), 9);
  }

  private static ActiveRunwayThreshold load(XElement source, ActiveRunway runway) {
    ActiveRunwayThreshold ret = new ActiveRunwayThreshold();
    ret.setParent(runway);
    ret.read(source);
    return ret;
  }

  private IList<Approach> approaches;
  private IList<DARoute> routes;
  private String name;
  private Coordinate coordinate;
  private double course;
  private int initialDepartureAltitude;
  private Coordinate estimatedFafPoint;
  private ActiveRunwayThreshold other;

  private ActiveRunwayThreshold() {
  }

  public IList<Approach> getApproaches() {
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

  public int getCourseInt() {
    return (int) Math.round(this.course);
  }

  public double getCourse() {
    return this.course;
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

  private void read(XElement source) {
    XmlLoader.setContext(source);
    this. name = XmlLoader.loadString("name");
    this. coordinate = XmlLoader.loadCoordinate("coordinate");
    this.initialDepartureAltitude = XmlLoader.loadInteger("initialDepartureAltitude");
    String mappingString = XmlLoader.loadString("mapping");
    IList<String> mapping = new EList<>(mappingString.split(";"));

    this.routes = this.getParent().getParent().getDaRoutes().where(q -> q.isMappingMatch(mapping));

    this.approaches = new EList<>();
    XmlLoader.loadList(
        source.getChild("approaches").getChildren(),
        this.approaches,
        q->Approach.load(q, this)
    );

    // adds visual approach if none exists
    if (this.approaches.isNone(q -> q.getType() == Approach.ApproachType.visual)) {
      Approach visual = Approach.generateDefaultVisualApproach(this);
      approaches.add(visual);
    }
  }
}
