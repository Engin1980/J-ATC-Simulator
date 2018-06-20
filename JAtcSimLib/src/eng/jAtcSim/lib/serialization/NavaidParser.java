package eng.jAtcSim.lib.serialization;

import eng.eSystem.eXml.XElement;
import eng.eSystem.xmlSerialization.IElementParser;
import eng.eSystem.xmlSerialization.XmlDeserializationException;
import eng.eSystem.xmlSerialization.XmlSerializationException;
import eng.eSystem.xmlSerialization.XmlSerializer;
import eng.jAtcSim.lib.world.Navaid;
import eng.jAtcSim.lib.world.NavaidList;

public class NavaidParser implements IElementParser<Navaid> {

  private NavaidList known;


  @Override
  public Class getType() {
    return Navaid.class;
  }

  @Override
  public Navaid parse(XElement xElement, XmlSerializer.Deserializer xmlSerializer) throws XmlDeserializationException {
    String name = xElement.getContent();
    Navaid ret = known.getOrGenerate(name);
    return ret;
  }

  @Override
  public void format(Navaid navaid, XElement xElement, XmlSerializer.Serializer xmlSerializer) throws XmlSerializationException {
    xElement.setContent(navaid.getName());
  }

  @Override
  public boolean isApplicableOnDescendants() {
    return false;
  }

  public void setRelative(NavaidList navaids) {
    this.known = navaids;
  }
}