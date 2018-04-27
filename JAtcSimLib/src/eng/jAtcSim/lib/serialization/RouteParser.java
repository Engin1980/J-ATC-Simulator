package eng.jAtcSim.lib.serialization;

import eng.eSystem.eXml.XElement;
import eng.eSystem.xmlSerialization.IElementParser;
import eng.eSystem.xmlSerialization.XmlDeserializationException;
import eng.eSystem.xmlSerialization.XmlSerializationException;
import eng.eSystem.xmlSerialization.XmlSerializer;
import eng.jAtcSim.lib.world.Route;

public class RouteParser implements IElementParser<Route> {
  @Override
  public Class getType() {
    return Route.class;
  }

  @Override
  public Route parse(XElement xElement, XmlSerializer.Deserializer deserializer) throws XmlDeserializationException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void format(Route route, XElement xElement, XmlSerializer.Serializer serializer) throws XmlSerializationException {
    xElement.setContent(route.getName());
  }

  @Override
  public boolean isApplicableOnDescendants() {
    return false;
  }
}
