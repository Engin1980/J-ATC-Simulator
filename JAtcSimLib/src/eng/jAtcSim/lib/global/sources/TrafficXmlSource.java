package eng.jAtcSim.lib.global.sources;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.xmlSerialization.XmlIgnore;
import eng.eSystem.xmlSerialization.XmlListItemMapping;
import eng.eSystem.xmlSerialization.XmlSerializer;
import eng.jAtcSim.lib.traffic.DensityBasedTraffic;
import eng.jAtcSim.lib.traffic.FlightListTraffic;
import eng.jAtcSim.lib.traffic.GenericTraffic;
import eng.jAtcSim.lib.traffic.Traffic;
import eng.jAtcSim.lib.world.Airport;
import eng.jAtcSim.lib.world.Area;

public class TrafficXmlSource extends XmlSource<IList<Traffic>> {

  //TODO duplicate with Traffic.eTrafficType
  public enum TrafficSource {
    activeAirportTraffic,
    xmlFileTraffic,
    specificTraffic
  }

  private Traffic activeTraffic;
  @XmlIgnore
  private Traffic specificTraffic;
  @XmlIgnore
  private Airport airport = null;

  public TrafficXmlSource(String xmlFile) {
    super(xmlFile);
  }

  public TrafficXmlSource(){super(null);}

  public Traffic getSpecificTraffic() {
    return specificTraffic;
  }

  public void setActiveTraffic(TrafficSource source, String trafficTitle) {
    switch (source){
      case activeAirportTraffic:
        activeTraffic = airport.getTrafficDefinitions().tryGetFirst(q->q.getTitle().equals(trafficTitle));
        if (activeTraffic == null)
          throw new EApplicationException("Unable to find traffic entitled as {" + trafficTitle + "} for airport {" + airport.getIcao() + "}.");
        break;
      case xmlFileTraffic:
        this.activeTraffic = this._get().tryGetFirst(q->q.getTitle().equals(trafficTitle));
        if (activeTraffic == null)
          throw new EApplicationException("Unable to find traffic entitled as {" + trafficTitle + "} in the traffic file {" + super.getXmlFileName() + "}");
        break;
      case specificTraffic:
        assert specificTraffic != null : "Specific traffic is null. ";
        this.activeTraffic = specificTraffic;
        break;
      default:
        throw new EEnumValueUnsupportedException(source);
    }
  }

  public Traffic getActiveTraffic() {
    return activeTraffic;
  }

  public void init(Airport airport, Traffic specificTraffic) {
    super.setInitialized();
    this.airport = airport;
    this.specificTraffic = specificTraffic;
  }

  @Override
  protected IList<Traffic> _load() {

    eng.eSystem.xmlSerialization.Settings sett = new eng.eSystem.xmlSerialization.Settings();

    // ignores
    sett.getIgnoredFieldsRegex().add("^_.+");
    sett.getIgnoredFieldsRegex().add("^parent$");
    sett.getIgnoredFieldsRegex().add("^binded$");
    sett.getIgnoredFieldsRegex().add("^scheduledMovements$");

    // list mappings
    sett.getListItemMappings().add(
        new XmlListItemMapping("/trafficDefinitions$", "genericTraffic", GenericTraffic.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/trafficDefinitions$", "densityTraffic", DensityBasedTraffic.class));
    sett.getListItemMappings().add(
        new XmlListItemMapping("/trafficDefinitions$", "flightListTraffic", FlightListTraffic.class));

    // own parsers
    sett.getValueParsers().add(new CoordinateValueParser());
//    sett.getValueParsers().add(new TrafficCategoryDefinitionParser());
    sett.getValueParsers().add(new IntParser());
    sett.getValueParsers().add(new IntegerParser());

    // instance creators

    // own loading
    XmlSerializer ser = new XmlSerializer(sett);
    IList<Traffic> ret = (IList) ser.deserialize(super.getXmlFileName(), EList.class);

    return ret;
  }
}
