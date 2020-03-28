package eng.jAtcSim.newLib.area;

import eng.eSystem.collections.EDistinctList;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.area.routes.DARoute;
import eng.jAtcSim.newLib.area.routes.GaRoute;
import eng.jAtcSim.newLib.area.routes.IafRoute;
import eng.jAtcSim.newLib.shared.exceptions.ToDoException;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;

public class Airport extends Parentable<Area> {

  static class XmlLoader {
    public static Airport load(XElement source, Area area) {
      Airport ret = new Airport();
      ret.setParent(area);
      read(source, ret);
      bindRoutes(ret);
      bindEntryExitPointsByRoutes(ret);
      return ret;
    }

    private static void read(XElement source, Airport airport) {
      XmlLoaderUtils.setContext(source);
      airport.icao = XmlLoaderUtils.loadString("icao");
      airport.name = XmlLoaderUtils.loadString("name");
      airport.altitude = XmlLoaderUtils.loadInteger("altitude");
      airport.transitionAltitude = XmlLoaderUtils.loadInteger("transitionAltitude");
      airport.vfrAltitude = XmlLoaderUtils.loadInteger("vfrAltitude");
      airport.declination = XmlLoaderUtils.loadDouble("declination");
      airport.coveredDistance = XmlLoaderUtils.loadInteger("coveredDistance");
      airport.mainAirportNavaid = airport.getParent().getNavaids().get(XmlLoaderUtils.loadString("mainAirportNavaidName"));
      airport.initialPosition = InitialPosition.XmlLoader.load(source.getChild("initialPosition"));
      airport.atcs = new EDistinctList<>(q -> q.getName(), EDistinctList.Behavior.exception);
      XmlLoaderUtils.loadList(
          source.getChild("atcTemplates").getChildren(),
          airport.atcs, q -> Atc.XmlLoader.load(q));
      {
        EList<EntryExitPoint> tmp = new EList<>();
        XmlLoaderUtils.loadList(
            source.getChild("entryExitPoints").getChildren(),
            tmp,
            q -> EntryExitPoint.XmlLoader.load(q, airport));
        airport.entryExitPoints = new EntryExitPointList(tmp);
      }
      airport.holds = new EList<>();
      XmlLoaderUtils.loadList(
          source.getChild("holds").getChildren(),
          airport.holds,
          q -> PublishedHold.XmlLoader.load(q, airport));

      airport.daRoutes = new EDistinctList<>(q -> q.getName(), EDistinctList.Behavior.exception);
      IReadOnlyList<XElement> routes = extractRoutes(source.getChild("daRoutes"), "route");
      XmlLoaderUtils.loadList(
          routes,
          airport.daRoutes,
          q -> DARoute.load(q, airport));

      airport.iafRoutes = new EList<>();
      routes = extractRoutes(source.getChild("iafRoutes"), "iafRoute");
      XmlLoaderUtils.loadList(
          routes,
          airport.iafRoutes,
          q -> IafRoute.load(q, airport));

      airport.gaRoutes = new EList<>();
      routes = extractRoutes(source.getChild("gaRoutes"), "gaRoute");
      XmlLoaderUtils.loadList(
          routes,
          airport.gaRoutes,
          q -> GaRoute.load(q, airport));

      // TODO put inactive and active runways to one upper element
      airport.inactiveRunways = new EList<>();
      XmlLoaderUtils.loadList(
          source.getChild("runways").getChildren("inactiveRunway"),
          airport.inactiveRunways,
          q -> InactiveRunway.XmlLoader.load(q, airport));

      airport.runways = new EList<>();
      XmlLoaderUtils.loadList(
          source.getChild("runways").getChildren("runway"),
          airport.runways,
          q -> ActiveRunway.XmlLoader.load(q, airport));

      airport.runwayConfigurations = new EList<>();
      XmlLoaderUtils.loadList(
          source.getChild("runwayConfigurations").getChildren(),
          airport.runwayConfigurations,
          q -> RunwayConfiguration.XmlLoader.load(q, airport));

      // TODO check airport
      // binding should be done in constructor
      // bind entryexitpointlist
      // bind holds
    }

    private static void bindEntryExitPointsByRoutes(Airport airport) {
      for (DARoute route : airport.daRoutes) {
        EntryExitPoint eep = EntryExitPoint.create(
            route.getMainNavaid(),
            route.getType() == DARoute.eType.sid ? EntryExitPoint.Type.exit : EntryExitPoint.Type.entry,
            route.getMaxMrvaAltitude());
        eep.setParent(airport);

        airport.entryExitPoints.add(eep);
      }
    }

    private static void bindRoutes(Airport airport) {
      //bind daRoutes, iafRoutes, gaRoutes by mappings
      //    ret.getAllThresholds().forEach(q -> ret.daRoutes.add(q.getRoutes())); // adds threshold specific routes
      throw new ToDoException();
    }

    private static IReadOnlyList<XElement> extractRoutes(XElement source, String lookForElementName) {
      IList<XElement> ret = new EList<>();

      ret.add(source.getChildren(lookForElementName));

      IReadOnlyList<XElement> groups = source.getChildren("group");
      groups.forEach(q -> ret.add(extractRoutes(q, lookForElementName)));

      return ret;
    }
  }

  private InitialPosition initialPosition;
  private IList<ActiveRunway> runways;
  private IList<InactiveRunway> inactiveRunways;
  private IList<Atc> atcs;
  private IList<PublishedHold> holds;
  private EntryExitPointList entryExitPoints;
  private String icao;
  private String name;
  private int altitude;
  private int transitionAltitude;
  private int vfrAltitude;
  private double declination;
  private Navaid mainAirportNavaid;
  private int coveredDistance;
  private IList<RunwayConfiguration> runwayConfigurations;
  private IList<DARoute> daRoutes;
  private IList<IafRoute> iafRoutes;
  private IList<GaRoute> gaRoutes;

  private Airport() {
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

}
