package eng.jAtcSim.newLib.area.serialization;

import eng.eSystem.eXml.XElement;
import eng.eSystem.xmlSerialization.XmlSerializer;
import eng.eSystem.xmlSerialization.supports.IElementParser;
import eng.jAtcSim.newLib.world.Navaid;
import eng.jAtcSim.newLib.world.NavaidList;

public class NavaidParser implements IElementParser<Navaid> {

  private NavaidList known;

  @Override
  public Navaid parse(XElement xElement, XmlSerializer.Deserializer xmlSerializer)  {
    String name = xElement.getContent();
    Navaid ret = known.getOrGenerate(name);
    return ret;
  }

  @Override
  public void format(Navaid navaid, XElement xElement, XmlSerializer.Serializer xmlSerializer)  {
    xElement.setContent(navaid.getName());
  }

  public void setRelative(NavaidList navaids) {
    this.known = navaids;
  }
}
