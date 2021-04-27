package eng.jAtcSim.newLib.area.global.newSources;

import eng.eSystem.eXml.XDocument;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.ApplicationException;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
import eng.jAtcSim.newLib.traffic.DensityBasedTraffic;
import eng.jAtcSim.newLib.traffic.FlightListTraffic;
import eng.jAtcSim.newLib.traffic.GenericTraffic;
import eng.jAtcSim.newLib.traffic.Traffic;

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
      throw new ApplicationException("Unable to load traffic from file '" + this.fileName + "'.", e);
    }

    super.setInitialized();
  }
}
