package eng.jAtcSim.lib.serialization;

import eng.eSystem.collections.IList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.xmlSerialization.IElementParser;
import eng.eSystem.xmlSerialization.XmlDeserializationException;
import eng.eSystem.xmlSerialization.XmlSerializationException;
import eng.eSystem.xmlSerialization.XmlSerializer;
import eng.jAtcSim.lib.world.Airport;
import eng.jAtcSim.lib.world.Route;
import eng.jAtcSim.lib.world.Runway;
import eng.jAtcSim.lib.world.RunwayThreshold;

public class RouteParser implements IElementParser<Route> {

  private Airport known;

  @Override
  public Class getType() {
    return Route.class;
  }

  @Override
  public Route parse(XElement xElement, XmlSerializer.Deserializer deserializer) throws XmlDeserializationException {
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
  public void format(Route route, XElement xElement, XmlSerializer.Serializer serializer) throws XmlSerializationException {
    xElement.setContent(route.getName());
  }

  @Override
  public boolean isApplicableOnDescendants() {
    return false;
  }

  public void setRelative(Airport aip) {
    this.known = aip;
  }
}
