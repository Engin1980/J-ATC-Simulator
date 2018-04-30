package eng.jAtcSim.lib.serialization;

import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.xmlSerialization.IElementParser;
import eng.eSystem.xmlSerialization.XmlDeserializationException;
import eng.eSystem.xmlSerialization.XmlSerializationException;
import eng.eSystem.xmlSerialization.XmlSerializer;
import eng.jAtcSim.lib.world.Airport;
import eng.jAtcSim.lib.world.Runway;

public class RunwayParser implements IElementParser<Runway> {


  private Airport known;

  @Override
  public Class getType() {
    return Runway.class;
  }

  @Override
  public Runway parse(XElement xElement, XmlSerializer.Deserializer xmlSerializer) throws XmlDeserializationException {
    String c = xElement.getContent();
    Runway ret = null;
    for (Runway runway : known.getRunways()) {
      if (runway.getName().equals(c)) {
        ret = runway;
        break;
      }
    }
    if (ret == null)
      throw new EApplicationException("Unable to find runway " + c + ".");
    return ret;
  }

  @Override
  public void format(Runway runway, XElement xElement, XmlSerializer.Serializer xmlSerializer) throws XmlSerializationException {
    xElement.setContent(runway.getName());
  }

  @Override
  public boolean isApplicableOnDescendants() {
    return false;
  }

  public void setRelative(Airport aip) {
    this.known = aip;
  }
}
