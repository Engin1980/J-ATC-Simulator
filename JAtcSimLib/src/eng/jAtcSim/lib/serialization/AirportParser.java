package eng.jAtcSim.lib.serialization;

import eng.eSystem.collections.IList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.xmlSerialization.IElementParser;
import eng.eSystem.xmlSerialization.XmlDeserializationException;
import eng.eSystem.xmlSerialization.XmlSerializationException;
import eng.eSystem.xmlSerialization.XmlSerializer;
import eng.jAtcSim.lib.global.KeyList;
import eng.jAtcSim.lib.world.Airport;

public class AirportParser implements IElementParser<Airport> {

  private IList<Airport> known;

  @Override
  public Class getType() {
    return Airport.class;
  }

  @Override
  public Airport parse(XElement xElement, XmlSerializer.Deserializer deserializer) throws XmlDeserializationException {
    Airport ret;
    String icao = xElement.getContent();
    ret = known.getFirst(q -> q.getIcao().equals(icao));
    return ret;
  }

  @Override
  public void format(Airport airport, XElement xElement, XmlSerializer.Serializer serializer) throws XmlSerializationException {
    xElement.setContent(airport.getIcao());
  }

  @Override
  public boolean isApplicableOnDescendants() {
    return false;
  }

  public void setRelative(KeyList<Airport,String> airports) {
    this.known = airports;
  }
}
