package eng.jAtcSim.newLib.area.xml;

import eng.eSystem.collections.EDistinctList;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.area.*;
import eng.jAtcSim.newLib.area.routes.DARoute;
import eng.jAtcSim.newLib.area.routes.GaRoute;
import eng.jAtcSim.newLib.area.routes.IafRoute;
import eng.jAtcSim.newLib.shared.exceptions.ToDoException;
import eng.jAtcSim.newLib.shared.xml.IXmlLoader;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;

public class AirportXmlLoader implements IXmlLoader<Airport> {



  private static IReadOnlyList<XElement> extractRoutes(XElement source, String lookForElementName) {
    IList<XElement> ret = new EList<>();

    ret.add(source.getChildren(lookForElementName));

    IReadOnlyList<XElement> groups = source.getChildren("group");
    groups.forEach(q -> ret.add(extractRoutes(q, lookForElementName)));

    return ret;
  }

  private final NavaidList navaids;
  private final IReadOnlyList<Border> borders;

  public AirportXmlLoader(NavaidList navaids, IReadOnlyList<Border> borders) {
    EAssert.Argument.isNotNull(navaids);
    EAssert.Argument.isNotNull(borders);
    this.navaids = navaids;
    this.borders = borders;
  }

  public Airport load(XElement source) {
    XmlLoaderUtils.setContext(source);
    String icao = XmlLoaderUtils.loadString("icao");
    String name = XmlLoaderUtils.loadString("name");
    int altitude = XmlLoaderUtils.loadInteger("altitude");
    int transitionAltitude = XmlLoaderUtils.loadInteger("transitionAltitude");
    int vfrAltitude = XmlLoaderUtils.loadInteger("vfrAltitude");
    double declination = XmlLoaderUtils.loadDouble("declination");
    int coveredDistance = XmlLoaderUtils.loadInteger("coveredDistance");
    Navaid mainAirportNavaid = navaids.get(
        XmlLoaderUtils.loadString("mainAirportNavaidName"));
    InitialPosition initialPosition = new InitialPositionXmlLoader().load(source.getChild("initialPosition"));

    IList<Atc> atcs = new EDistinctList<>(q -> q.getName(), EDistinctList.Behavior.exception);
    XmlLoaderUtils.loadList(
        source.getChild("atcTemplates").getChildren(),
        atcs,
        new AtcXmlLoader());

    EntryExitPointList entryExitPoints;
    {
      IList<EntryExitPoint> tmp = XmlLoaderUtils.loadList(
          source.getChild("entryExitPoints").getChildren(),
          new EntryExitPointXmlLoader(navaids, mainAirportNavaid, borders));
      entryExitPoints = new EntryExitPointList(tmp);
    }
    IList<PublishedHold> holds = XmlLoaderUtils.loadList(
        source.getChild("holds").getChildren(),
        new PublishedHoldXmlLoader(navaids));


    IList<DARoute> daRoutes = new EDistinctList<>(q -> q.getName(), EDistinctList.Behavior.exception);
    IReadOnlyList<XElement> routes = extractRoutes(source.getChild("daRoutes"), "route");
    XmlLoaderUtils.loadList(routes, daRoutes, new DARouteXmlLoader(navaids));

    routes = extractRoutes(source.getChild("iafRoutes"), "iafRoute");
    IList<IafRoute> iafRoutes = XmlLoaderUtils.loadList(
        routes,
        new IafRouteXmlLoader(navaids));

    routes = extractRoutes(source.getChild("gaRoutes"), "gaRoute");
    IList<GaRoute> gaRoutes = XmlLoaderUtils.loadList(
        routes,
        new GaRouteXmlLoader(navaids));

    // TODO put inactive and active runways to one upper element
    IList<InactiveRunway> inactiveRunways = XmlLoaderUtils.loadList(
        source.getChild("runways").getChildren("inactiveRunway"),
        new InactiveRunwayXmlLoader());

    IList<ActiveRunway> runways = XmlLoaderUtils.loadList(
        source.getChild("runways").getChildren("runway"),
        new ActiveRunwayXmlLoader(daRoutes));

    IList<RunwayConfiguration> runwayConfigurations = XmlLoaderUtils.loadList(
        source.getChild("runwayConfigurations").getChildren(),
        new RunwayConfigurationXmlLoader(navaids, runways));

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
