/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.world;

import eng.eSystem.collections.*;
import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.lib.atcs.AtcTemplate;

/**
 * @author Marek
 */
public class Airport {

  private final InitialPosition initialPosition;
  private final IList<ActiveRunway> runways;
  private final IList<InactiveRunway> inactiveRunways;
  private final IList<AtcTemplate> atcTemplates;
  private final IList<PublishedHold> holds;
  private final EntryExitPointList entryExitPoints;
  private final String icao;
  private final String name;
  private final int altitude;
  private final int transitionAltitude;
  private final int vfrAltitude;
  private final double declination;
  private final Navaid mainAirportNavaid;
  private final Area parent;
  private final int coveredDistance;
  private final IList<RunwayConfiguration> runwayConfigurations;
  private final IList<Route> routes;

  public Airport(String icao, String name, Navaid mainAirportNavaid, int altitude, int vfrAltitude, int transitionAltitude, int coveredDistance, double declination, InitialPosition initialPosition, IList<AtcTemplate> atcTemplates, IList<ActiveRunway> runways, IList<InactiveRunway> inactiveRunways, IList<PublishedHold> holds, IList<EntryExitPoint> entryExitPoints, IList<RunwayConfiguration> runwayConfigurations, IMap<String, IList<Route>> sharedRoutesGroups, Area parent) {
    this.initialPosition = initialPosition;
    this.runways = runways;
    this.inactiveRunways = inactiveRunways;
    this.atcTemplates = atcTemplates;
    this.holds = holds;
    this.entryExitPoints = new EntryExitPointList(entryExitPoints);
    this.icao = icao;
    this.name = name;
    this.altitude = altitude;
    this.transitionAltitude = transitionAltitude;
    this.vfrAltitude = vfrAltitude;
    this.declination = declination;
    this.mainAirportNavaid = mainAirportNavaid;
    this.parent = parent;
    this.coveredDistance = coveredDistance;
    this.runwayConfigurations = runwayConfigurations;

    this.routes = new EDistinctList<>(EDistinctList.Behavior.exception);
    sharedRoutesGroups.forEach(e -> this.routes.add(e.getValue())); // adds shared routes
    this.getAllThresholds().forEach(q -> this.routes.add(q.getRoutes())); // adds threshold specific routes

    bindEntryExitPointsByRoutes();
  }

  public void bindEntryExitPointsByRoutes() {
    for (Route route : this.routes) {
      EntryExitPoint eep = new EntryExitPoint(
          this,
          route.getMainNavaid(),
          route.getType() == Route.eType.sid ? EntryExitPoint.Type.exit : EntryExitPoint.Type.entry,
          route.getMaxMrvaAltitude());

      this.entryExitPoints.add(eep);
    }
  }

  public IReadOnlyList<EntryExitPoint> getEntryExitPoints() {
    return entryExitPoints.toReadOnlyList();
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

  public IReadOnlyList<ActiveRunway> getRunways() {
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

  public IReadOnlyList<PublishedHold> getHolds() {
    return holds;
  }

  public ActiveRunwayThreshold tryGetRunwayThreshold(String runwayThresholdName) {
    for (ActiveRunway r : runways) {
      for (ActiveRunwayThreshold t : r.getThresholds()) {
        if (t.getName().equals(runwayThresholdName)) {
          return t;
        }
      }
    }
    return null;
  }

  public ActiveRunwayThreshold getRunwayThreshold(String runwayThresholdName) {
    ActiveRunwayThreshold ret = tryGetRunwayThreshold(runwayThresholdName);
    if (ret == null)
      throw new RuntimeException(String.format("Unable to find threshold '%s' for airport '%s'.", runwayThresholdName, this.icao));
    return ret;
  }

  public IReadOnlyList<AtcTemplate> getAtcTemplates() {
    return atcTemplates;
  }

  public int getVfrAltitude() {
    return vfrAltitude;
  }

  public Navaid getMainAirportNavaid() {
    return this.mainAirportNavaid;
  }

  public IReadOnlyList<ActiveRunwayThreshold> getAllThresholds() {
    IList<ActiveRunwayThreshold> ret = new EList<>();
    for (ActiveRunway runway : this.getRunways()) {
      for (ActiveRunwayThreshold threshold : runway.getThresholds()) {
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
}
