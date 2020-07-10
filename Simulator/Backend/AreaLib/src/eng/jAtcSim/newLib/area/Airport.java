package eng.jAtcSim.newLib.area;

import eng.eSystem.collections.EDistinctList;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.ToDoException;
import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.area.routes.DARoute;
import eng.jAtcSim.newLib.area.routes.GaRoute;
import eng.jAtcSim.newLib.area.routes.IafRoute;
import eng.jAtcSim.newLib.shared.enums.DARouteType;

public class Airport extends Parentable<Area> {

  private static void checkAllCategoriesForRunwayConfigurationsAreApplied(
      IList<RunwayThresholdConfiguration> departures,
      IList<RunwayThresholdConfiguration> arrivals) {
    //TODO this must be called after binding
    IList<ActiveRunway> rwys = new EDistinctList<>(EDistinctList.Behavior.skip);
    rwys.add(arrivals.select(q -> q.getThreshold().getParent()).distinct());
    rwys.add(departures.select(q -> q.getThreshold().getParent()).distinct());

    // check if all categories are applied
    for (char i = 'A'; i <= 'D'; i++) {
      char c = i;
      if (!arrivals.isAny(q -> q.isForCategory(c)))
        throw new EApplicationException("Unable to find arrival threshold for category " + c);
      if (!departures.isAny(q -> q.isForCategory(c)))
        throw new EApplicationException("Unable to find departure threshold for category " + c);
    }
  }

  private final int altitude;
  private final IList<Atc> atcs;
  private final int coveredDistance;
  private final IList<DARoute> daRoutes;
  private final double declination;
  private final EntryExitPointList entryExitPoints;
  private final IList<GaRoute> gaRoutes;
  private final IList<PublishedHold> holds;
  private final IList<IafRoute> iafRoutes;
  private final String icao;
  private final IList<InactiveRunway> inactiveRunways;
  private final InitialPosition initialPosition;
  private final Navaid mainAirportNavaid;
  private final String name;
  private final IList<RunwayConfiguration> runwayConfigurations;
  private final IList<ActiveRunway> runways;
  private final int transitionAltitude;
  private final int vfrAltitude;

  public Airport(String icao, String name, Navaid mainAirportNavaid,
                 int altitude, int transitionAltitude, int vfrAltitude, double declination,
                 InitialPosition initialPosition, int coveredDistance,
                 IList<ActiveRunway> runways, IList<InactiveRunway> inactiveRunways,
                 IList<Atc> atcs,
                 IList<PublishedHold> holds, EntryExitPointList customEntryExitPoints, IList<RunwayConfiguration> runwayConfigurations,
                 IList<DARoute> daRoutes, IList<IafRoute> iafRoutes, IList<GaRoute> gaRoutes) {
    this.initialPosition = initialPosition;
    this.runways = runways;
    this.inactiveRunways = inactiveRunways;
    this.atcs = atcs;
    this.holds = holds;
    this.entryExitPoints = customEntryExitPoints;
    this.icao = icao;
    this.name = name;
    this.altitude = altitude;
    this.transitionAltitude = transitionAltitude;
    this.vfrAltitude = vfrAltitude;
    this.declination = declination;
    this.mainAirportNavaid = mainAirportNavaid;
    this.coveredDistance = coveredDistance;
    this.runwayConfigurations = runwayConfigurations;
    // TODO the following should be checked for every runway configuration
    //checkAllCategoriesForRunwayConfigurationsAreApplied()
    this.daRoutes = daRoutes;
    this.iafRoutes = iafRoutes;
    this.gaRoutes = gaRoutes;
  }

  public void bind() {
    this.inactiveRunways.forEach(q->{
      q.setParent(this);
      q.bind();
    });
    this.runways.forEach(q->{
      q.setParent(this);
      q.bind();
    });
    this.runwayConfigurations.forEach(q -> q.bind());
    this.bindEntryExitPointsByRoutes();
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

  public int getAltitude() {
    return altitude;
  }

  public IReadOnlyList<Atc> getAtcTemplates() {
    return atcs;
  }

  public int getCoveredDistance() {
    return coveredDistance;
  }

  public IReadOnlyList<DARoute> getDaRoutes() {
    return daRoutes;
  }

  public double getDeclination() {
    return declination;
  }

  public IReadOnlyList<EntryExitPoint> getEntryExitPoints() {
    return entryExitPoints.toReadOnlyList();
  }

  public IReadOnlyList<GaRoute> getGaRoutes() {
    return this.gaRoutes;
  }

  public IReadOnlyList<PublishedHold> getHolds() {
    return holds;
  }

  public IReadOnlyList<IafRoute> getIafRoutes() {
    return this.iafRoutes;
  }

  public String getIcao() {
    return icao;
  }

  public IReadOnlyList<InactiveRunway> getInactiveRunways() {
    return inactiveRunways;
  }

  public InitialPosition getInitialPosition() {
    return this.initialPosition;
  }

  public Coordinate getLocation() {
    return runways.get(0).getThresholdA().getCoordinate();
  }

  public Navaid getMainAirportNavaid() {
    return this.mainAirportNavaid;
  }

  public String getName() {
    return name;
  }

  public IReadOnlyList<RunwayConfiguration> getRunwayConfigurations() {
    return runwayConfigurations;
  }

  public ActiveRunwayThreshold getRunwayThreshold(String runwayThresholdName) {
    ActiveRunwayThreshold ret = tryGetRunwayThreshold(runwayThresholdName);
    if (ret == null)
      throw new RuntimeException(String.format("Unable to find threshold '%s' for airport '%s'.", runwayThresholdName, this.icao));
    return ret;
  }

  public IReadOnlyList<ActiveRunway> getRunways() {
    return runways;
  }

  public int getTransitionAltitude() {
    return transitionAltitude;
  }

  public int getVfrAltitude() {
    return vfrAltitude;
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

  private void bindEntryExitPointsByRoutes() {
    for (DARoute route : this.daRoutes) {
      EntryExitPoint eep = EntryExitPoint.create(
          route.getMainNavaid(),
          route.getType() == DARouteType.sid ? EntryExitPoint.Type.exit : EntryExitPoint.Type.entry,
          route.getMaxMrvaAltitude());
      eep.setParent(this);

      this.entryExitPoints.add(eep);
    }
  }

}
