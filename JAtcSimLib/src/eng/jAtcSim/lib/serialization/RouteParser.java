package eng.jAtcSim.lib.serialization;

import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.xmlSerialization.XmlSerializer;
import eng.eSystem.xmlSerialization.supports.IElementParser;
import eng.jAtcSim.lib.world.Airport;
import eng.jAtcSim.lib.world.Route;
import eng.jAtcSim.lib.world.Runway;
import eng.jAtcSim.lib.world.RunwayThreshold;

public class RouteParser implements IElementParser<Route> {

  private Airport known;

  @Override
  public Route parse(XElement xElement, XmlSerializer.Deserializer deserializer)  {
    String c = xElement.getContent();
Route ret = null;

    for (Runway runway : known.getRunways()) {
      for (RunwayThreshold threshold : runway.getThresholds()) {
        for (Route route : threshold.getRoutes()) {
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
  public void format(Route route, XElement xElement, XmlSerializer.Serializer serializer) {
    xElement.setContent(route.getName());
  }

  public void setRelative(Airport aip) {
    this.known = aip;
  }
}
