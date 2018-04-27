package eng.jAtcSim.lib.serialization;

import eng.eSystem.eXml.XElement;
import eng.eSystem.xmlSerialization.IElementParser;
import eng.eSystem.xmlSerialization.XmlDeserializationException;
import eng.eSystem.xmlSerialization.XmlSerializationException;
import eng.eSystem.xmlSerialization.XmlSerializer;
import eng.jAtcSim.lib.world.Runway;

public class RunwayParser implements IElementParser<Runway> {
  @Override
  public Class getType() {
    return Runway.class;
  }

  @Override
  public Runway parse(XElement xElement, XmlSerializer.Deserializer xmlSerializer) throws XmlDeserializationException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void format(Runway runway, XElement xElement, XmlSerializer.Serializer xmlSerializer) throws XmlSerializationException {
    xElement.setContent(runway.getName());
  }

  @Override
  public boolean isApplicableOnDescendants() {
    return false;
  }
}
