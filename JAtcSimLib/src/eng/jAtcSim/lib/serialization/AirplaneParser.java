package eng.jAtcSim.lib.serialization;

import eng.eSystem.eXml.XElement;
import eng.eSystem.xmlSerialization.IElementParser;
import eng.eSystem.xmlSerialization.XmlDeserializationException;
import eng.eSystem.xmlSerialization.XmlSerializationException;
import eng.eSystem.xmlSerialization.XmlSerializer;
import eng.jAtcSim.lib.airplanes.Airplane;

public class AirplaneParser implements IElementParser<Airplane> {

//  private IMap<String, Airplane> known = new EMap<>();

  @Override
  public Class getType() {
    return Airplane.class;
  }

  @Override
  public void format(Airplane airplane, XElement xElement, XmlSerializer.Serializer source) throws XmlSerializationException {

//    if (known.containsKey(airplane.getCallsign().toString())) {
      xElement.setContent(airplane.getCallsign().toString());
//    } else {
//      known.set(airplane.getCallsign().toString(), airplane);
//      source.serialize(airplane, xElement);
//    }
  }

  @Override
  public boolean isApplicableOnDescendants() {
    return false;
  }

  @Override
  public Airplane parse(XElement elm, XmlSerializer.Deserializer source) throws XmlDeserializationException {
    throw new UnsupportedOperationException();
//    Airplane ret;
//
//    String c = elm.getContent();
//    if (StringUtils.isNullOrEmpty(c) == false) {
//      ret = known.get(c);
//    } else {
//      ret = (Airplane) source.deserialize(elm, Airplane.class);
//    }
//
//    return ret;
  }
}
