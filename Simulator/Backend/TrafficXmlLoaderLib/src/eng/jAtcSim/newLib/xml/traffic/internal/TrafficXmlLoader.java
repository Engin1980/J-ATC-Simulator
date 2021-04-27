package eng.jAtcSim.newLib.xml.traffic.internal;

import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.ApplicationException;
import eng.eSystem.exceptions.UnexpectedValueException;
import eng.jAtcSim.newLib.traffic.ITrafficModel;

public class TrafficXmlLoader {
  public static ITrafficModel load(XElement root) {
    ITrafficModel ret;
    try {
      XElement source = root.getChildren().getFirst(q -> !q.getName().equals("meta"));

      switch (source.getName()) {
        case "genericTraffic":
          ret = new SimpleGenericTrafficModelXmlLoader().load(source);
          break;
        case "densityTraffic":
          ret = new DensityBasedTrafficModelXmlLoader().load(source);
          break;
        case "flightListTraffic":
          ret = new FlightListTraffixModelXmlLoader().load(source);
          break;
        default:
          throw new UnexpectedValueException(source.getName());
      }
    } catch (Exception e) {
      throw new ApplicationException("Unable to load traffic model.", e);
    }

    return ret;
  }
}
