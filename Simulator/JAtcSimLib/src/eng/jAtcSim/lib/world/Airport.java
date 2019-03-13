/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.world;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.ERuntimeException;
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
  private final IList<EntryExitPoint> entryExitPoints;
  private String icao;
  private String name;
  private int altitude;
  private int transitionAltitude;
  private int vfrAltitude = -1;
  private double declination;
  private Navaid mainAirportNavaid;
  private Area parent;
  private int coveredDistance;
  private IList<RunwayConfiguration> runwayConfigurations;
  private IList<Route> routes;

  public Airport(String icao, String name, Navaid mainAirportNavaid, int altitude, int vfrAltitude, int transitionAltitude, int coveredDistance, double declination, InitialPosition initialPosition, IList<AtcTemplate> atcTemplates, IList<ActiveRunway> runways, IList<InactiveRunway> inactiveRunways, IList<PublishedHold> holds, IList<EntryExitPoint> entryExitPoints, IList<RunwayConfiguration> runwayConfigurations, IList<Route> routes, Area parent) {
    this.initialPosition = initialPosition;
    this.runways = runways;
    this.inactiveRunways = inactiveRunways;
    this.atcTemplates = atcTemplates;
    this.holds = holds;
    this.entryExitPoints = entryExitPoints;
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
    this.routes = routes;
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

  public void setParent(Area parent) {
    this.parent = parent;
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

  public ActiveRunwayThreshold getRunwayThreshold(String runwayThresholdName){
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
    if (this.mainAirportNavaid == null) {
      try {
        this.mainAirportNavaid = this.getParent().getNavaids().get(this.mainAirportNavaidName);
      } catch (ERuntimeException ex) {
        throw new EApplicationException("Failed to find main navaid named " + this.mainAirportNavaidName + " for aiport " + this.name + ". Invalid area file?", ex);
      }
    }

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
