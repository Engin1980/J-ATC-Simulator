package eng.jAtcSim.lib.serialization;

import eng.eSystem.collections.IList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.xmlSerialization.XmlSerializer;
import eng.eSystem.xmlSerialization.supports.IElementParser;
import eng.jAtcSim.lib.airplanes.Airplane;

public class AirplaneParser implements IElementParser<Airplane> {

private IList<Airplane> known;

  @Override
  public void format(Airplane airplane, XElement xElement, XmlSerializer.Serializer source) {

      xElement.setContent(airplane.getFlightModule().getCallsign().toString());
  }

  @Override
  public Airplane parse(XElement elm, XmlSerializer.Deserializer source) {

    Airplane ret;
    String c = elm.getContent();
    ret = known.getFirst(q->q.getFlightModule().getCallsign().toString().equals(c));
    return ret;
  }

  public void setRelative(IList<Airplane> lst) {
    this.known = lst;
  }
}
