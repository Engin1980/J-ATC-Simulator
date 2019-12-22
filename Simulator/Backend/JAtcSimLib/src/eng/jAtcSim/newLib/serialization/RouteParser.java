package eng.jAtcSim.newLib.area.serialization;

import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.xmlSerialization.XmlSerializer;
import eng.eSystem.xmlSerialization.supports.IElementParser;
import eng.jAtcSim.newLib.world.Airport;
import eng.jAtcSim.newLib.world.DARoute;
import eng.jAtcSim.newLib.world.ActiveRunway;
import eng.jAtcSim.newLib.world.ActiveRunwayThreshold;

public class RouteParser implements IElementParser<DARoute> {

  private Airport known;

  @Override
  public DARoute parse(XElement xElement, XmlSerializer.Deserializer deserializer)  {
    String c = xElement.getContent();
DARoute ret = null;

    for (ActiveRunway runway : known.getRunways()) {
      for (ActiveRunwayThreshold threshold : runway.getThresholds()) {
        for (DARoute route : threshold.getRoutes()) {
          if (route.getName().equals(c)){
            ret = route;
            break;
          }
        }
      }
    }
    if (ret == null)
      throw new EApplicationException("Unable to find route " + c + " for airport " + known.getName());

    return ret;
  }

  @Override
  public void format(DARoute route, XElement xElement, XmlSerializer.Serializer serializer) {
    xElement.setContent(route.getName());
  }

  public void setRelative(Airport aip) {
    this.known = aip;
  }
}
