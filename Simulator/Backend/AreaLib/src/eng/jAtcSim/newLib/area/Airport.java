package eng.jAtcSim.newLib.area;

import eng.eSystem.collections.EDistinctList;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.routes.DARoute;
import eng.jAtcSim.newLib.routes.GaRoute;
import eng.jAtcSim.newLib.routes.IafRoute;
import eng.jAtcSim.newLib.shared.exceptions.ToDoException;
import eng.jAtcSim.newLib.shared.xml.XmlLoader;

public class Airport extends Parentable<Area> {

  public static Airport load(XElement source, Area area) {
    Airport ret = new Airport();
    ret.setParent(area);
    ret.read(source);
    ret.bindRoutes();
    ret.bindEntryExitPointsByRoutes();
    return ret;
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

  private void bindEntryExitPointsByRoutes() {
    for (DARoute route : this.daRoutes) {
      EntryExitPoint eep = EntryExitPoint.create(
          route.getMainNavaid(),
          route.getType() == DARoute.eType.sid ? EntryExitPoint.Type.exit : EntryExitPoint.Type.entry,
          route.getMaxMrvaAltitude());
      eep.setParent(this);

      this.entryExitPoints.add(eep);
    }
  }

  private void bindRoutes() {
    //bind daRoutes, iafRoutes, gaRoutes by mappings
    //    ret.getAllThresholds().forEach(q -> ret.daRoutes.add(q.getRoutes())); // adds threshold specific routes
    throw new ToDoException();
  }

  private IReadOnlyList<XElement> extractRoutes(XElement source, String lookForElementName) {
    IList<XElement> ret = new EList<>();

    ret.add(source.getChildren(lookForElementName));

    IReadOnlyList<XElement> groups = source.getChildren("group");
    groups.forEach(q -> ret.add(extractRoutes(q, lookForElementName)));

    return ret;
  }

  private void read(XElement source) {
    XmlLoader.setContext(source);
    this.icao = XmlLoader.loadString("icao");
    this.name = XmlLoader.loadString("name");
    this.altitude = XmlLoader.loadInteger("altitude");
    this.transitionAltitude = XmlLoader.loadInteger("transitionAltitude");
    this.vfrAltitude = XmlLoader.loadInteger("vfrAltitude");
    this.declination = XmlLoader.loadDouble("declination");
    this.coveredDistance = XmlLoader.loadInteger("coveredDistance");
    this.mainAirportNavaid = this.getParent().getNavaids().get(XmlLoader.loadString("mainAirportNavaidName"));
    this.initialPosition = InitialPosition.load(source.getChild("initialPosition"));
    this.atcs = new EDistinctList<>(q -> q.getName(), EDistinctList.Behavior.exception);
    XmlLoader.loadList(
        source.getChild("atcTemplates").getChildren(),
        this.atcs, q -> Atc.load(q));
    {
      EList<EntryExitPoint> tmp = new EList<>();
      XmlLoader.loadList(
          source.getChild("entryExitPoints").getChildren(),
          tmp,
          q -> EntryExitPoint.load(q, this));
      this.entryExitPoints = new EntryExitPointList(tmp);
    }
    this.holds = new EList<>();
    XmlLoader.loadList(
        source.getChild("holds").getChildren(),
        this.holds,
        q -> PublishedHold.load(q, this));

    this.daRoutes = new EDistinctList<>(q -> q.getName(), EDistinctList.Behavior.exception);
    IReadOnlyList<XElement> routes = extractRoutes(source.getChild("daRoutes"), "route");
    XmlLoader.loadList(
        routes,
        this.daRoutes,
        q -> DARoute.load(q, this));

    this.iafRoutes = new EList<>();
    routes = extractRoutes(source.getChild("iafRoutes"), "iafRoute");
    XmlLoader.loadList(
        routes,
        this.iafRoutes,
        q -> IafRoute.load(q, this));

    this.gaRoutes = new EList<>();
    routes = extractRoutes(source.getChild("gaRoutes"), "gaRoute");
    XmlLoader.loadList(
        routes,
        this.gaRoutes,
        q -> GaRoute.load(q, this));

    // TODO put inactive and active runways to one upper element
    this.inactiveRunways = new EList<>();
    XmlLoader.loadList(
        source.getChild("runways").getChildren("inactiveRunway"),
        this.inactiveRunways,
        q -> InactiveRunway.load(q, this));

    this.inactiveRunways = new EList<>();
    XmlLoader.loadList(
        source.getChild("runways").getChildren("runway"),
        this.runways,
        q -> ActiveRunway.load(q, this));

    this.runwayConfigurations = new EList<>();
    XmlLoader.loadList(
        source.getChild("runwayConfigurations").getChildren(),
        this.runwayConfigurations,
        q -> RunwayConfiguration.load(q, this));

    // TODO check this
    // binding should be done in constructor
    // bind entryexitpointlist
    // bind holds
  }
}
