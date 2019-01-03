package eng.jAtcSim.lib.serialization;

import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.xmlSerialization.XmlSerializer;
import eng.eSystem.xmlSerialization.supports.IElementParser;
import eng.jAtcSim.lib.world.Airport;
import eng.jAtcSim.lib.world.Runway;

public class RunwayParser implements IElementParser<Runway> {
  private Airport known;

  @Override
  public Runway parse(XElement xElement, XmlSerializer.Deserializer xmlSerializer)  {
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
  public void format(Runway runway, XElement xElement, XmlSerializer.Serializer xmlSerializer)   {
    xElement.setContent(runway.getName());
  }

  public void setRelative(Airport aip) {
    this.known = aip;
  }
}
