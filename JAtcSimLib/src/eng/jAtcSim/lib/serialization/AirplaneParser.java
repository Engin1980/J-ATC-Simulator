package eng.jAtcSim.lib.serialization;

import eng.eSystem.collections.IList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.xmlSerialization.IElementParser;
import eng.eSystem.xmlSerialization.XmlDeserializationException;
import eng.eSystem.xmlSerialization.XmlSerializationException;
import eng.eSystem.xmlSerialization.XmlSerializer;
import eng.jAtcSim.lib.airplanes.Airplane;

public class AirplaneParser implements IElementParser<Airplane> {

private IList<Airplane> known;

  @Override
  public Class getType() {
    return Airplane.class;
  }

  @Override
  public void format(Airplane airplane, XElement xElement, XmlSerializer.Serializer source) throws XmlSerializationException {

      xElement.setContent(airplane.getCallsign().toString());
  }

  @Override
  public boolean isApplicableOnDescendants() {
    return false;
  }

  @Override
  public Airplane parse(XElement elm, XmlSerializer.Deserializer source) throws XmlDeserializationException {

    Airplane ret;
    String c = elm.getContent();
    ret = known.getFirst(q->q.getCallsign().toString().equals(c));
    return ret;
  }

  public void setRelative(IList<Airplane> lst) {
    this.known = lst;
  }
}
