package eng.jAtcSim.newLib.xml.area.internal;

import eng.eSystem.collections.EDistinctList;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.area.*;
import eng.jAtcSim.newLib.area.routes.DARoute;
import eng.jAtcSim.newLib.area.routes.GaRoute;
import eng.jAtcSim.newLib.area.routes.IafRoute;
import eng.jAtcSim.newLib.shared.xml.SmartXmlLoaderUtils;
import eng.jAtcSim.newLib.xml.area.internal.context.LoadingContext;
import eng.jAtcSim.newLib.xml.area.internal.routes.DARouteXmlLoader;
import eng.jAtcSim.newLib.xml.area.internal.routes.GaRouteXmlLoader;
import eng.jAtcSim.newLib.xml.area.internal.routes.IafRouteXmlLoader;

import static eng.eSystem.utilites.FunctionShortcuts.coalesce;

class AirportXmlLoader extends XmlLoader<Airport> {

  private static IReadOnlyList<XElement> extractRoutes(XElement source, String lookForElementName) {
    IList<XElement> ret = new EList<>();

    ret.addMany(source.getChildren(lookForElementName));

    IReadOnlyList<XElement> groups = source.getChildren("group");
    groups.forEach(q -> ret.addMany(extractRoutes(q, lookForElementName)));

    return ret;
  }

  AirportXmlLoader(LoadingContext context) {
    super(context);
  }

  public Airport load(XElement source) {
    log(1, "Xml-loading airport");
    SmartXmlLoaderUtils.setContext(source);
    String icao = SmartXmlLoaderUtils.loadString("icao");
    log(1, "... airport '%s'", icao);
    String name = SmartXmlLoaderUtils.loadString("name");
    int altitude = SmartXmlLoaderUtils.loadInteger("altitude");
    int transitionAltitude = SmartXmlLoaderUtils.loadInteger("transitionAltitude");
    int vfrAltitude = SmartXmlLoaderUtils.loadInteger("vfrAltitude", -1);
    if (vfrAltitude == -1) vfrAltitude = altitude + 2500;
    double declination = SmartXmlLoaderUtils.loadDouble("declination");
    int coveredDistance = SmartXmlLoaderUtils.loadInteger("coveredDistance");
    Navaid mainAirportNavaid = context.area.navaids.get(
        SmartXmlLoaderUtils.loadString("mainAirportNavaidName"));
    InitialPosition initialPosition = new InitialPositionXmlLoader().load(source.getChild("initialPosition"));

    context.area.navaids.registerDeclination(mainAirportNavaid.getCoordinate(), declination);
    context.airport.icao = icao;
    context.airport.mainNavaid = mainAirportNavaid;
    context.airport.altitude = altitude;
    context.airport.declination = declination;
    context.airport.daMappings = new XmlMappingDictinary<>();
    context.airport.iafMappings = new XmlMappingDictinary<>();
    context.airport.gaMappings = new XmlMappingDictinary<>();

    IList<Atc> atcs = new EDistinctList<>(q -> q.getName(), EDistinctList.Behavior.exception);
    SmartXmlLoaderUtils.loadList(
        source.getChild("atcTemplates").getChildren(),
        atcs,
        new AtcXmlLoader());

    EntryExitPointList entryExitPoints;
    {
      IList<EntryExitPoint> tmp = SmartXmlLoaderUtils.loadList(
          coalesce(source.tryGetChild("entryExitPoints"), new XElement("tmp")).getChildren(),
          new EntryExitPointXmlLoader(context));
      entryExitPoints = new EntryExitPointList(tmp);
    }
    IList<PublishedHold> holds = SmartXmlLoaderUtils.loadList(
        source.getChild("holds").getChildren(),
        new PublishedHoldXmlLoader(context));

    IList<DARoute> daRoutes = new EDistinctList<>(q -> q.getName(), EDistinctList.Behavior.exception);
    IReadOnlyList<XElement> routes = extractRoutes(source.getChild("daRoutes"), "route");
    SmartXmlLoaderUtils.loadList(routes, daRoutes, new DARouteXmlLoader(context));

    routes = extractRoutes(source.getChild("iafRoutes"), "iafRoute");
    IList<IafRoute> iafRoutes = SmartXmlLoaderUtils.loadList(routes, new IafRouteXmlLoader(context));

    routes = extractRoutes(source.getChild("gaRoutes"), "gaRoute");
    IList<GaRoute> gaRoutes = SmartXmlLoaderUtils.loadList(routes, new GaRouteXmlLoader(context));

    IList<InactiveRunway> inactiveRunways = SmartXmlLoaderUtils.loadList(
        source.getChild("runways").getChildren("inactiveRunway"),
        new InactiveRunwayXmlLoader());

    IList<ActiveRunway> runways = SmartXmlLoaderUtils.loadList(
        source.getChild("runways").getChildren("runway"),
        new ActiveRunwayXmlLoader(context));
    context.airport.activeRunways = runways;

    IList<RunwayConfiguration> runwayConfigurations = new EList<>();
    {
      XElement rc = source.tryGetChild("runwayConfigurations");
      if (rc != null)
        SmartXmlLoaderUtils.loadList(
            rc.getChildren(),
            runwayConfigurations,
            new RunwayConfigurationXmlLoader(context));
    }

    // TODO check airport
    // binding should be done in constructor
    // bind entryexitpointlist
    // bind holds

    Airport ret = new Airport(icao, name, mainAirportNavaid, altitude, transitionAltitude, vfrAltitude, declination,
        initialPosition, coveredDistance,
        runways, inactiveRunways,
        atcs, holds, entryExitPoints,
        runwayConfigurations,
        daRoutes, iafRoutes, gaRoutes);
    return ret;
  }
}
