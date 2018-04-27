package eng.jAtcSim.lib.serialization;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.eSystem.eXml.XElement;
import eng.eSystem.utilites.StringUtils;
import eng.eSystem.xmlSerialization.IElementParser;
import eng.eSystem.xmlSerialization.XmlDeserializationException;
import eng.eSystem.xmlSerialization.XmlSerializationException;
import eng.eSystem.xmlSerialization.XmlSerializer;
import eng.jAtcSim.lib.atcs.Atc;

public class AtcParser implements IElementParser<Atc> {

//  private final IMap<String, Atc> known = new EMap<>();

  @Override
  public Class getType() {
    return Atc.class;
  }

  @Override
  public Atc parse(XElement xElement, XmlSerializer.Deserializer xmlSerializer) throws XmlDeserializationException {
//    Atc ret;
//    String c = xElement.getContent();
//    if (StringUtils.isNullOrEmpty(c) == false) {
//      ret = known.get(c);
//    } else {
//      ret = (Atc) xmlSerializer.deserialize(xElement, Atc.class);
//    }
//    return ret;
    throw new UnsupportedOperationException();
  }

  @Override
  public void format(Atc atc, XElement xElement, XmlSerializer.Serializer xmlSerializer) throws XmlSerializationException {
    String c = atc.getName();
//    if (known.containsKey(c)) {
      xElement.setContent(c);
//    } else {
//      known.set(c, atc);
//      xmlSerializer.serialize(c, xElement);
//    }
  }

  @Override
  public boolean isApplicableOnDescendants() {
    return true;
  }
}
