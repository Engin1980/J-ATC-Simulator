package eng.jAtcSim.lib.global.sources;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.xmlSerialization.*;
import eng.jAtcSim.lib.atcs.AtcTemplate;
import eng.jAtcSim.lib.coordinates.Coordinate;
import eng.jAtcSim.lib.global.PlaneCategoryDefinitions;
import eng.jAtcSim.lib.traffic.DensityBasedTraffic;
import eng.jAtcSim.lib.traffic.FlightListTraffic;
import eng.jAtcSim.lib.traffic.GenericTraffic;
import eng.jAtcSim.lib.world.*;
import eng.jAtcSim.lib.world.approaches.GnssApproach;
import eng.jAtcSim.lib.world.approaches.IafRoute;
import eng.jAtcSim.lib.world.approaches.IlsApproach;
import eng.jAtcSim.lib.world.approaches.UnpreciseApproach;

public class AreaXmlSource extends XmlSource<Area> {

  private int activeAirportIndex = 0;

  public AreaXmlSource(String xmlFile) {
    super(xmlFile);
  }

  public AreaXmlSource() {
    super(null);
  }

  public int getActiveAirportIndex() {
    return activeAirportIndex;
  }

  public Airport getActiveAirport() {
    Airport ret = super.getContent().getAirports().get(this.activeAirportIndex);
    return ret;
  }

  public void setActiveAirport(String icao) {
    this.activeAirportIndex =
        super.getContent().getAirports().getIndexOf(q -> q.getIcao().equals(icao));
  }

  public void init(String icao) {
    super.setInitialized();
    super.getContent().init();
    this.setActiveAirport(icao);
  }

  public void init(int index) {
    super.setInitialized();
    super.getContent().init();
    this.setActiveAirport(super.getContent().getAirports().get(index).getIcao());
  }

  @Override
  protected Area _load() {
    eng.eSystem.xmlSerialization.Settings sett = new eng.eSystem.xmlSerialization.Settings();

    // ignores
    sett.getIgnoredFieldsRegex().add("^_.+");
    sett.getIgnoredFieldsRegex().add("^parent$");
    sett.getIgnoredFieldsRegex().add("^binded$");
    sett.getIgnoredFieldsRegex().add("^scheduledMovements$");

    // list mappings
    sett.getListItemMappings().add(
        new XmlListItemMapping("/airports$", Airport.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/runways$", Runway.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/runways/runway/thresholds$", RunwayThreshold.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/inactiveRunways$", InactiveRunway.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/inactiveRunways/runway/thresholds$", InactiveRunwayThreshold.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/approaches$", "ilsApproach", IlsApproach.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/approaches$", "unpreciseApproach", UnpreciseApproach.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/approaches$", "gnssApproach", GnssApproach.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/ilsApproach/categories$", IlsApproach.Category.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/iafRoutes$", IafRoute.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/sharedIafRoutes$", IafRoute.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/routes$", Route.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/sharedRoutes$", Route.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/atcTemplates$", AtcTemplate.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/holds$", PublishedHold.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/entryExitPoints$", EntryExitPoint.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/vfrPoints$", VfrPoint.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/navaids$", Navaid.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/borders$", Border.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/points$", "point", BorderExactPoint.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/points$", "arc", BorderArcPoint.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/points$", "crd", BorderCrdPoint.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/companies$", DensityBasedTraffic.CodeWeight.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/countries$", DensityBasedTraffic.CodeWeight.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/density$", DensityBasedTraffic.HourBlockMovements.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/directions$", DensityBasedTraffic.DirectionWeight.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/trafficDefinitions$", "genericTraffic", GenericTraffic.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/trafficDefinitions$", "densityTraffic", DensityBasedTraffic.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/trafficDefinitions$", "flightListTraffic", FlightListTraffic.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/disjoints$", String.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/runwayConfigurations$", RunwayConfiguration.class));

    // own parsers
    sett.getValueParsers().add(new CoordinateValueParser());
    sett.getValueParsers().add(new TrafficCategoryDefinitionParser());
    sett.getValueParsers().add(new IntParser());
    sett.getValueParsers().add(new IntegerParser());
    sett.getElementParsers().add(new RunwayConfigurationParser());

    // instance creators
    sett.getInstanceCreators().add(new AreaCreator());


    // own loading
    XmlSerializer ser = new XmlSerializer(sett);
    Area ret = (Area) ser.deserialize(super.getXmlFileName(), Area.class);

    return ret;
  }
}


class AreaCreator implements eng.eSystem.xmlSerialization.IInstanceCreator<Area> {

  @Override
  public Class getType() {
    return Area.class;
  }

  @Override
  public Area createInstance() {
    Area ret = Area.create();
    return ret;
  }
}

class CoordinateValueParser implements IValueParser<Coordinate> {

  @Override
  public Class getType() {
    return Coordinate.class;
  }

  @Override
  public Coordinate parse(String s) {
    return Coordinate.parseNew(s);
  }

  @Override
  public String format(Coordinate coordinate) {
    throw new UnsupportedOperationException();
  }
}

class TrafficCategoryDefinitionParser implements IValueParser<PlaneCategoryDefinitions> {

  @Override
  public Class getType() {
    return PlaneCategoryDefinitions.class;
  }

  @Override
  public PlaneCategoryDefinitions parse(String s) throws XmlDeserializationException {
    PlaneCategoryDefinitions ret = new PlaneCategoryDefinitions(s);
    return ret;
  }

  @Override
  public String format(PlaneCategoryDefinitions trafficCategoryDefinition) throws XmlSerializationException {
    return trafficCategoryDefinition.toString();
  }
}

class IntParser implements IValueParser<Integer> {

  @Override
  public Class getType() {
    return int.class;
  }

  @Override
  public Integer parse(String s) throws XmlDeserializationException {
    Integer ret;
    if (s.startsWith("FL")) {
      s = s.substring(2);
      ret = Integer.parseInt(s);
      ret = ret * 100;
    } else {
      ret = Integer.parseInt(s);
    }
    return ret;
  }

  @Override
  public String format(Integer value) throws XmlSerializationException {
    return value.toString();
  }
}

class IntegerParser implements IValueParser<Integer> {

  @Override
  public Class getType() {
    return Integer.class;
  }

  @Override
  public Integer parse(String s) throws XmlDeserializationException {
    Integer ret;
    if (s.startsWith("FL")) {
      s = s.substring(2);
      ret = Integer.parseInt(s);
      ret = ret * 100;
    } else {
      ret = Integer.parseInt(s);
    }
    return ret;
  }

  @Override
  public String format(Integer value) throws XmlSerializationException {
    return value.toString();
  }
}

class RunwayConfigurationParser implements IElementParser<RunwayConfiguration> {

  @Override
  public Class getType() {
    return RunwayConfiguration.class;
  }

  @Override
  public RunwayConfiguration parse(XElement xElement, XmlSerializer.Deserializer deserializer) throws XmlDeserializationException {
    int windFrom = 0;
    int windTo = 359;
    int windSpeedFrom = 0;
    int windSpeedTo = 999;
    IList<RunwayConfiguration.RunwayThresholdConfiguration> arrivals = new EList<>();
    IList<RunwayConfiguration.RunwayThresholdConfiguration> departures = new EList<>();

    String tmp;
    tmp = xElement.tryGetAttribute("windFrom");
    if (tmp != null) windFrom = Integer.parseInt(tmp);
    tmp = xElement.tryGetAttribute("windTo");
    if (tmp != null) windTo = Integer.parseInt(tmp);
    tmp = xElement.tryGetAttribute("windSpeedFrom");
    if (tmp != null) windSpeedFrom = Integer.parseInt(tmp);
    tmp = xElement.tryGetAttribute("windSpeedTo");
    if (tmp != null) windSpeedTo = Integer.parseInt(tmp);

    RunwayConfiguration.RunwayThresholdConfiguration rtc;
    for (XElement elm : xElement.getChildren()) {
      rtc = (RunwayConfiguration.RunwayThresholdConfiguration)
          deserializer.deserialize(elm, RunwayConfiguration.RunwayThresholdConfiguration.class);
      if (elm.getName().equals("arrivals"))
        arrivals.add(rtc);
      else if (elm.getName().equals("departures"))
        departures.add(rtc);
    }

    RunwayConfiguration ret = new RunwayConfiguration(windFrom, windTo, windSpeedFrom, windSpeedTo, arrivals, departures);
    return ret;
  }

  @Override
  public void format(RunwayConfiguration runwayConfiguration, XElement xElement, XmlSerializer.Serializer serializer) throws XmlSerializationException {
    throw new UnsupportedOperationException("This method is not expected to be called.");
  }

  @Override
  public boolean isApplicableOnDescendants() {
    return false;
  }
}