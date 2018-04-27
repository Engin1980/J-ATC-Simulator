package eng.jAtcSim.lib.serialization;

import eng.eSystem.eXml.XElement;
import eng.eSystem.xmlSerialization.IElementParser;
import eng.eSystem.xmlSerialization.XmlDeserializationException;
import eng.eSystem.xmlSerialization.XmlSerializationException;
import eng.eSystem.xmlSerialization.XmlSerializer;
import eng.jAtcSim.lib.airplanes.AirplaneType;
import eng.jAtcSim.lib.airplanes.AirplaneTypes;

public class AirplaneTypeParser implements IElementParser<AirplaneType> {

//  private AirplaneTypes known;

//  public AirplaneTypeParser(AirplaneTypes known) {
//    this.known = known;
//  }

  @Override
  public Class getType() {
    return AirplaneType.class;
  }

  @Override
  public AirplaneType parse(XElement xElement, XmlSerializer.Deserializer xmlSerializer) throws XmlDeserializationException {
    AirplaneType ret;
    String name = xElement.getContent();
    throw new UnsupportedOperationException();
//    ret = known.tryGetByName(name);
//    if (ret == null)
//      throw new XmlDeserializationException("Failed to find airplane type name %s.", name);
//    return ret;
  }

  @Override
  public void format(AirplaneType airplaneType, XElement xElement, XmlSerializer.Serializer xmlSerializer) throws XmlSerializationException {
    xElement.setContent(airplaneType.name);
  }

  @Override
  public boolean isApplicableOnDescendants() {
    return false;
  }
}
