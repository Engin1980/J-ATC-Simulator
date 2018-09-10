package eng.jAtcSim.lib.serialization;

import eng.eSystem.collections.EList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.xmlSerialization.XmlSerializer;
import eng.eSystem.xmlSerialization.supports.IElementParser;
import eng.jAtcSim.lib.atcs.Atc;

public class AtcParser implements IElementParser<Atc> {

  private EList<Atc> known;

  @Override
  public Atc parse(XElement xElement, XmlSerializer.Deserializer xmlSerializer)   {
    Atc ret;
    String c = xElement.getContent();
    ret = known.getFirst(q->q.getName().equals(c));
    return ret;
  }

  @Override
  public void format(Atc atc, XElement xElement, XmlSerializer.Serializer xmlSerializer)   {
    String c = atc.getName();
    xElement.setContent(c);
  }

  public void setRelative(EList<Atc> atcs) {
    this.known = atcs;
  }
}
