/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.world;

import eng.eSystem.collections.EDistinctList;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.ERuntimeException;
import eng.eSystem.xmlSerialization.XmlIgnore;
import eng.eSystem.xmlSerialization.XmlOptional;
import eng.jAtcSim.lib.atcs.AtcTemplate;
import eng.jAtcSim.lib.coordinates.Coordinate;
import eng.jAtcSim.lib.traffic.Traffic;
import eng.jAtcSim.lib.world.approaches.Approach;
import eng.jAtcSim.lib.world.approaches.IafRoute;

/**
 * @author Marek
 */
public class Airport {

  public static class SharedRoutesGroup {
    public String groupName;
    public IList<Route> routes = new EList<>();
  }

  public static class SharedIafRoutesGroup {
    public String groupName;
    public IList<IafRoute> iafRoutes = new EList<>();
  }

  private final InitialPosition initialPosition = new InitialPosition();
  private final IList<Runway> runways = new EList<>();
  @XmlOptional
  private final IList<InactiveRunway> inactiveRunways = new EList<>();
  private final IList<AtcTemplate> atcTemplates = new EList<>();
  private final IList<PublishedHold> holds = new EList<>();
  @XmlOptional
  private final IList<EntryExitPoint> entryExitPoints = new EList<>();
  private String icao;
  private String name;
  private int altitude;
  private int transitionAltitude;
  @XmlOptional
  private int vfrAltitude = -1;
  private String mainAirportNavaidName;
  private double declination;
  private Navaid _mainAirportNavaid;
  private IList<Traffic> trafficDefinitions = new EList<>();
  private Area parent;
  private int coveredDistance;
  @XmlOptional
  private IList<RunwayConfiguration> runwayConfigurations = new EList<>();

  @XmlOptional
  private IList<SharedRoutesGroup> sharedRoutesGroups = new EList<>();
  @XmlOptional
  private IList<SharedIafRoutesGroup> sharedIafRoutesGroups = new EList<>();
  @XmlIgnore
  private IList<Route> routes;

  public IReadOnlyList<SharedRoutesGroup> getSharedRoutesGroups() {
    return sharedRoutesGroups;
  }

  public IReadOnlyList<SharedIafRoutesGroup> getSharedIafRoutesGroups() {
    return sharedIafRoutesGroups;
  }

  public IReadOnlyList<EntryExitPoint> getEntryExitPoints() {
    return entryExitPoints;
  }

  public int getCoveredDistance() {
    return coveredDistance;
  }

  public double getDeclination() {
    return declination;
  }

  public String getIcao() {
    return icao;
  }

  public String getName() {
    return name;
  }

  public int getTransitionAltitude() {
    return transitionAltitude;
  }

  public Coordinate getLocation() {
    return runways.get(0).getThresholdA().getCoordinate();
  }

  public IReadOnlyList<Runway> getRunways() {
    return runways;
  }

  public IReadOnlyList<InactiveRunway> getInactiveRunways() {
    return inactiveRunways;
  }

  public int getAltitude() {
    return altitude;
  }

  public Area getParent() {
    return parent;
  }

  public void setParent(Area parent) {
    this.parent = parent;
  }

  public IReadOnlyList<PublishedHold> getHolds() {
    return holds;
  }

  public IReadOnlyList<Traffic> getTrafficDefinitions() {
    return trafficDefinitions;
  }

  public RunwayThreshold tryGetRunwayThreshold(String runwayThresholdName) {
    for (Runway r : runways) {
      for (RunwayThreshold t : r.getThresholds()) {
        if (t.getName().equals(runwayThresholdName)) {
          return t;
        }
      }
    }
    return null;
  }

  public IReadOnlyList<AtcTemplate> getAtcTemplates() {
    return atcTemplates;
  }

  public int getVfrAltitude() {
    return vfrAltitude;
  }

  public Navaid getMainAirportNavaid() {
    if (this._mainAirportNavaid == null) {
      try {
        this._mainAirportNavaid = this.getParent().getNavaids().get(this.mainAirportNavaidName);
      } catch (ERuntimeException ex) {
        throw new EApplicationException("Failed to find main navaid named " + this.mainAirportNavaidName + " for aiport " + this.name + ". Invalid area file?", ex);
      }
    }

    return this._mainAirportNavaid;
  }

  public IReadOnlyList<RunwayThreshold> getAllThresholds() {
    IList<RunwayThreshold> ret = new EList<>();
    for (Runway runway : this.getRunways()) {
      for (RunwayThreshold threshold : runway.getThresholds()) {
        ret.add(threshold);
      }
    }
    return ret;
  }

  public IReadOnlyList<RunwayConfiguration> getRunwayConfigurations() {
    return runwayConfigurations;
  }

  public InitialPosition getInitialPosition() {
    return this.initialPosition;
  }

  public IReadOnlyList<Route> getRoutes() {
    return routes;
  }

  public void bind() {
    Airport a = this;

    for (PublishedHold h : a.getHolds()) {
      h.bind();
    }

    // fill routes list
    this.routes = new EDistinctList<>(EDistinctList.Behavior.exception);
    this.sharedRoutesGroups.forEach(q->this.routes.add(q.routes)); // adds shared routes
    this.getAllThresholds().forEach(q-> this.routes.add(q.getRoutes())); // adds threshold specific routes

    for (Route o : a.getRoutes()) {
      o.bind();
    }
    a.bindEntryExitPointsByRoutes();

    for (Runway r : a.getRunways()) {
      for (RunwayThreshold t : r.getThresholds()) {
        t.bind();

        for (Approach p : t.getApproaches()) {
          p.bind();
        }
      }

      for (EntryExitPoint eep : a.getEntryExitPoints()) {
        eep.bind();
      }
    }

    for (RunwayConfiguration runwayConfiguration : a.getRunwayConfigurations()) {
      runwayConfiguration.bind();
    }

  }

  private void bindEntryExitPointsByRoutes() {
    for (Route route : this.routes) {
      EntryExitPoint eep = new EntryExitPoint(
          route.getMainNavaid(),
          route.getType() == Route.eType.sid ? EntryExitPoint.Type.exit : EntryExitPoint.Type.entry,
          route.getMaxMrvaAltitude());

      mergeEntryExitPoints(eep);
    }
  }

  private void mergeEntryExitPoints(EntryExitPoint eep) {
    EntryExitPoint tmp = this.getEntryExitPoints().tryGetFirst(q -> q.getName().equals(eep.getNavaid().getName()));
    if (tmp == null)
      this.entryExitPoints.add(eep);
    else {
      tmp.adjustBy(eep);
    }
  }
}
