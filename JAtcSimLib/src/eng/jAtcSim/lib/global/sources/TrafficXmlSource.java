package eng.jAtcSim.lib.global.sources;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;

import eng.eSystem.utilites.ExceptionUtils;
import eng.eSystem.xmlSerialization.XmlSerializer;
import eng.eSystem.xmlSerialization.XmlSettings;
import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
import eng.jAtcSim.lib.traffic.DensityBasedTraffic;
import eng.jAtcSim.lib.traffic.FlightListTraffic;
import eng.jAtcSim.lib.traffic.GenericTraffic;
import eng.jAtcSim.lib.traffic.Traffic;
import eng.jAtcSim.lib.world.Airport;
import eng.jAtcSim.lib.world.xml.AltitudeValueParser;

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

    XmlSettings sett = new XmlSettings();

    // list mappings
    sett.getMeta().registerXmlItemElement(
        EList.class, "genericTraffic", GenericTraffic.class, false, null );
    sett.getMeta().registerXmlItemElement(
        EList.class, "densityTraffic", DensityBasedTraffic.class, false, null );
    sett.getMeta().registerXmlItemElement(
        EList.class, "flightListTraffic", FlightListTraffic.class, false, null);
    sett.getMeta().registerXmlItemIgnoredElement(EList.class, "meta");

    sett.getMeta().registerCustomParser(int.class, false, new AltitudeValueParser());
    sett.getMeta().registerCustomParser(Integer.class, false, new AltitudeValueParser());

    // own loading
    XmlSerializer ser = new XmlSerializer(sett);
    IList tmp = ser.deserialize(super.getXmlFileName(), EList.class);

    IList<Traffic> ret = tmp.where(q-> q instanceof Traffic);

    return ret;
  }
}
