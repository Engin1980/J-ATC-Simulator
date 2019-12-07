package eng.jAtcSim.newLib.serialization;

import eng.eSystem.collections.IList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.xmlSerialization.XmlSerializer;
import eng.eSystem.xmlSerialization.supports.IElementParser;
import eng.jAtcSim.newLib.world.Airport;

public class AirportParser implements IElementParser<Airport> {

  private IList<Airport> known;

  @Override
  public Airport parse(XElement xElement, XmlSerializer.Deserializer deserializer)   {
    Airport ret;
    String icao = xElement.getContent();
    ret = known.getFirst(q -> q.getIcao().equals(icao));
    return ret;
  }

  @Override
  public void format(Airport airport, XElement xElement, XmlSerializer.Serializer serializer)   {
    xElement.setContent(airport.getIcao());
  }

  public void setRelative(IList<Airport> airports) {
    this.known = airports;
  }
}
