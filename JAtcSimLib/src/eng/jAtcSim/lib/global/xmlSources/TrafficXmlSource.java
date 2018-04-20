package eng.jAtcSim.lib.global.xmlSources;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.xmlSerialization.XmlIgnore;
import eng.jAtcSim.lib.traffic.Traffic;
import eng.jAtcSim.lib.world.Airport;

public class TrafficXmlSource extends XmlSource<IList<Traffic>> {

  public enum TrafficSource {
    activeAirportTraffic,
    xmlFileTraffic,
    specificTraffic
  }

  private IList<Traffic> specificTraffic;
  @XmlIgnore
  private Airport airport = null;
  private int activeTrafficIndex = 0;
  private TrafficSource source = TrafficSource.activeAirportTraffic;

  public TrafficXmlSource(String xmlFile) {
    super(xmlFile);
  }

  public void setActiveTraffic(TrafficSource source, int activeTrafficIndexInSource) {
    this.source = source;
    this.activeTrafficIndex = activeTrafficIndexInSource;

    switch (source){
      case activeAirportTraffic:
        assert airport.getTrafficDefinitions().size() > this.activeTrafficIndex;
        break;
      case xmlFileTraffic:
        assert super.getContent().size() > this.activeTrafficIndex;
      case specificTraffic:
        assert this.specificTraffic.size() > this.activeTrafficIndex;
      default:
        throw new EEnumValueUnsupportedException(source);
    }
  }

  public Traffic getActiveTraffic() {
    Traffic ret;
    switch (source) {
      case activeAirportTraffic:
        ret = airport.getTrafficDefinitions().get(activeTrafficIndex);
        break;
      case xmlFileTraffic:
        ret = super.getContent().get(activeTrafficIndex);
        break;
      case specificTraffic:
        ret = this.specificTraffic.get(activeTrafficIndex);
        break;
      default:
        throw new EEnumValueUnsupportedException(source);
    }
    return ret;
  }

  public void init(Airport airport, Traffic ... specificTraffic) {
    super.setInitialized();
    this.airport = airport;
    this.specificTraffic = new EList<>();

    IList<Traffic> tmp = new EList<>(specificTraffic);
    tmp = tmp.where(q->q != null);
    this.specificTraffic.add(tmp);
  }

  @Override
  protected IList<Traffic> _load() {
    System.out.println("\tTraffic from xml loading is not supported yet.");
    return new EList<>();
  }
}
