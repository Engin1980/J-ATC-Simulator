package eng.jAtcSim.lib.global.newSources;

import eng.eSystem.eXml.XDocument;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.jAtcSim.lib.traffic.DensityBasedTraffic;
import eng.jAtcSim.lib.traffic.FlightListTraffic;
import eng.jAtcSim.lib.traffic.GenericTraffic;
import eng.jAtcSim.lib.traffic.Traffic;

public class XmlTrafficSource extends TrafficSource {

  @XmlIgnore
  private Traffic traffic;
  private String fileName;

  public XmlTrafficSource(String fileName)
  {
    this.fileName = fileName;
  }

  @Override
  protected Traffic _getContent() {
    return traffic;
  }

  @Override
  public void init() {
    try {
      XDocument doc = XDocument.load(this.fileName);
      XElement source = doc.getRoot().getChildren().getFirst(q->!q.getName().equals("meta"));

      switch (source.getName()){
        case "genericTraffic":
          this.traffic = GenericTraffic.load(source);
          break;
        case "densityTraffic":
          this.traffic = DensityBasedTraffic.load(source);
          break;
        case "flightListTraffic":
          this.traffic = FlightListTraffic.load(source);
          break;
        default:
          throw new EEnumValueUnsupportedException(source.getName());
      }
    } catch (Exception e) {
      throw new EApplicationException("Unable to load traffic from file '" + this.fileName + "'.", e);
    }

    super.setInitialized();
  }
}
