package eng.jAtcSim.newLib.area.serialization;

import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.ApplicationException;
import eng.eSystem.xmlSerialization.XmlSerializer;
import eng.eSystem.xmlSerialization.supports.IElementParser;
import eng.jAtcSim.newLib.world.Airport;
import eng.jAtcSim.newLib.world.ActiveRunway;

public class RunwayParser implements IElementParser<ActiveRunway> {
  private Airport known;

  @Override
  public ActiveRunway parse(XElement xElement, XmlSerializer.Deserializer xmlSerializer)  {
    String c = xElement.getContent();
    ActiveRunway ret = null;
    for (ActiveRunway runway : known.getRunways()) {
      if (runway.getName().equals(c)) {
        ret = runway;
        break;
      }
    }
    if (ret == null)
      throw new ApplicationException("Unable to find runway " + c + ".");
    return ret;
  }

  @Override
  public void format(ActiveRunway runway, XElement xElement, XmlSerializer.Serializer xmlSerializer)   {
    xElement.setContent(runway.getName());
  }

  public void setRelative(Airport aip) {
    this.known = aip;
  }
}
