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
import eng.jAtcSim.lib.atcs.AtcTemplate;
import eng.jAtcSim.lib.world.approaches.GaRoute;
import eng.jAtcSim.lib.world.approaches.IafRoute;
import eng.jAtcSim.lib.world.xml.XmlLoader;

/**
 * @author Marek
 */
public class Airport extends Parentable<Area> {

  public static Airport load(XElement source, NavaidList navaids,
                             IReadOnlyList<Border> borders) {
    XmlLoader.setContext(source);
    String icao = XmlLoader.loadString("icao");
    String name = XmlLoader.loadString("name");
    int altitude = XmlLoader.loadInteger("altitude");
    int transitionAltitude = XmlLoader.loadInteger("transitionAltitude");
    int vfrAltitude = XmlLoader.loadInteger("vfrAltitude");
    double declination = XmlLoader.loadDouble("declination");
    int coveredDistance = XmlLoader.loadInteger("coveredDistance");
    String mainAirportNavaidName = XmlLoader.loadString("mainAirportNavaidName");

    InitialPosition initialPosition = InitialPosition.load(source.getChild("initialPosition"));
    IList<AtcTemplate> atcTemplates = AtcTemplate.loadList(source.getChild("atcTemplates").getChildren());
    Navaid mainAirportNavaid = navaids.get(mainAirportNavaidName);
    EntryExitPointList entryExitPointList = EntryExitPoint.loadList(source.getChild("entryExitPoints").getChildren(), navaids);
    IList<PublishedHold> holds = PublishedHold.loadList(source.getChild("holds").getChildren(), navaids);

    IList<DARoute> daRoutes = DARoute.loadList(source.getChild("daRoutes"), navaids, holds,
        borders.where(q -> q.getType() == Border.eType.mrva));
    IList<IafRoute> iafRoutes = IafRoute.loadList(source.getChild("iafRoutes"), navaids, holds);
    IList<GaRoute> gaRoutes = GaRoute.loadList(source.getChild("gaRoutes"), navaids, holds);

    // TODO put inactive and active runways to one upper element
    IList<InactiveRunway> inactiveRunways = InactiveRunway.loadList(source.getChild("runways").getChildren("inactiveRunway"));
    IList<ActiveRunway> activeRunways = ActiveRunway.loadList(source.getChild("runways").getChildren("runway"),
        altitude, navaids, daRoutes, iafRoutes, gaRoutes);

    IList<RunwayConfiguration> runwayConfigurations = RunwayConfiguration.loadList(
        source.getChild("runwayConfigurations").getChildren(),
        activeRunways);

    // binding should be done in constructor
    // bind entryexitpointlist
    // bind holds

    Airport ret = new Airport(icao, name, mainAirportNavaid, altitude,
        vfrAltitude, transitionAltitude, coveredDistance,
        declination, initialPosition, atcTemplates,
        activeRunways, inactiveRunways,
        holds, entryExitPointList, runwayConfigurations,
        daRoutes, iafRoutes, gaRoutes);

    return ret;
  }

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
  private final int coveredDistance;
  private final IList<RunwayConfiguration> runwayConfigurations;
  private final IList<DARoute> routes;
  private final IList<IafRoute> iafRoutes;
  private final IList<GaRoute> gaRoutes;

  private Airport(String icao, String name, Navaid mainAirportNavaid, int altitude, int vfrAltitude, int transitionAltitude,
                  int coveredDistance, double declination, InitialPosition initialPosition,
                  IList<AtcTemplate> atcTemplates, IList<ActiveRunway> runways, IList<InactiveRunway> inactiveRunways,
                  IList<PublishedHold> holds, EntryExitPointList entryExitPoints, IList<RunwayConfiguration> runwayConfigurations,
                  IList<DARoute> daRoutes, IList<IafRoute> iafRoutes, IList<GaRoute> gaRoutes) {
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
    this.coveredDistance = coveredDistance;
    this.runwayConfigurations = runwayConfigurations;
    this.routes = daRoutes;
    this.iafRoutes = iafRoutes;
    this.gaRoutes = gaRoutes;

    this.runways.forEach(q->q.setParent(this));
    this.inactiveRunways.forEach(q->q.setParent(this));
    this.holds.forEach(q->q.setParent(this));
    this.routes.forEach(q->q.setParent(this));
    this.iafRoutes.forEach(q->q.setParent(this));
    this.gaRoutes.forEach(q->q.setParent(this));

    this.getAllThresholds().forEach(q -> this.routes.add(q.getRoutes())); // adds threshold specific routes
    bindEntryExitPointsByRoutes();
  }

  public void bindEntryExitPointsByRoutes() {
    for (DARoute route : this.routes) {
      EntryExitPoint eep = EntryExitPoint.create(
          route.getMainNavaid(),
          route.getType() == DARoute.eType.sid ? EntryExitPoint.Type.exit : EntryExitPoint.Type.entry,
          route.getMaxMrvaAltitude());
      eep.setParent(this);

      this.entryExitPoints.add(eep);
    }
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

  public IReadOnlyList<AtcTemplate> getAtcTemplates() {
    return atcTemplates;
  }

  public int getCoveredDistance() {
    return coveredDistance;
  }

  public double getDeclination() {
    return declination;
  }

  public IReadOnlyList<EntryExitPoint> getEntryExitPoints() {
    return entryExitPoints.toReadOnlyList();
  }

  public IReadOnlyList<PublishedHold> getHolds() {
    return holds;
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

  public IReadOnlyList<DARoute> getRoutes() {
    return routes;
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
}
