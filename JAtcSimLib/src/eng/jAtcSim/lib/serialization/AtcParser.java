package eng.jAtcSim.lib.serialization;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.eSystem.eXml.XElement;
import eng.eSystem.xmlSerialization.IElementParser;
import eng.eSystem.xmlSerialization.XmlDeserializationException;
import eng.eSystem.xmlSerialization.XmlSerializationException;
import eng.eSystem.xmlSerialization.XmlSerializer;
import eng.jAtcSim.lib.atcs.Atc;

public class AtcParser implements IElementParser<Atc> {

  private EList<Atc> known;

  @Override
  public Class getType() {
    return Atc.class;
  }

  @Override
  public Atc parse(XElement xElement, XmlSerializer.Deserializer xmlSerializer) throws XmlDeserializationException {
    Atc ret;
    String c = xElement.getContent();
    ret = known.getFirst(q->q.getName().equals(c));
    return ret;
  }

  @Override
  public void format(Atc atc, XElement xElement, XmlSerializer.Serializer xmlSerializer) throws XmlSerializationException {
    String c = atc.getName();
    xElement.setContent(c);
  }

  @Override
  public boolean isApplicableOnDescendants() {
    return true;
  }

  public void setRelative(EList<Atc> atcs) {
    this.known = atcs;
  }
}
