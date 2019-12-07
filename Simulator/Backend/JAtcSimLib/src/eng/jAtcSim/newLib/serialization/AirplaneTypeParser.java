package eng.jAtcSim.newLib.serialization;

import eng.eSystem.eXml.XElement;
import eng.eSystem.xmlSerialization.XmlSerializer;
import eng.eSystem.xmlSerialization.exceptions.XmlSerializationException;
import eng.eSystem.xmlSerialization.supports.IElementParser;
import eng.jAtcSim.newLib.airplanes.AirplaneType;
import eng.jAtcSim.newLib.airplanes.AirplaneTypes;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class AirplaneTypeParser implements IElementParser<AirplaneType> {

 private AirplaneTypes known;


  @Override
  public AirplaneType parse(XElement xElement, XmlSerializer.Deserializer xmlSerializer)  {
    AirplaneType ret;
    String name = xElement.getContent();
    ret = known.tryGetByName(name);
    if (ret == null)
      throw new XmlSerializationException(sf("Failed to find airplane kind name %s.", name));
    return ret;
  }

  @Override
  public void format(AirplaneType airplaneType, XElement xElement, XmlSerializer.Serializer xmlSerializer)   {
    xElement.setContent(airplaneType.name);
  }

  public void setRelative(AirplaneTypes types) {
    this.known = types;
  }
}
