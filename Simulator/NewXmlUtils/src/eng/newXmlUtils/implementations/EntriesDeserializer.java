package eng.newXmlUtils.implementations;

import eng.newXmlUtils.base.Deserializer;
import eng.newXmlUtils.EXmlException;
import eng.newXmlUtils.XmlContext;
import eng.newXmlUtils.utils.InternalXmlUtils;
import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.eSystem.eXml.XElement;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class EntriesDeserializer implements Deserializer {



  @Override
  public Object invoke(XElement e, XmlContext c) {
    Class<?> type = InternalXmlUtils.loadType(e);
    IMap map = new EMap();
    for (XElement child : e.getChildren(InternalXmlUtils.ENTRY)) {

      Object key = loadFromElement(child.getChild(InternalXmlUtils.KEY), c);
      Object value = loadFromElement(child.getChild(InternalXmlUtils.VALUE), c);

      map.set(key, value);
    }

    Object ret = createInstance(type, map);
    return ret;
  }

  private Object createInstance(Class<?> type, IMap map) {
    Object ret;
    if (EMap.class.equals(type))
      ret = map;
    else
      throw new EXmlException(sf("Unsupported map-to conversion to '%s'.", type));
    return ret;
  }

  private Object loadFromElement(XElement child, XmlContext c) {
    Class<?> itemType = InternalXmlUtils.loadType(child);
    Deserializer deserializer = c.sdfManager.getDeserializer(itemType);
    Object ret = deserializer.invoke(child, c);
    return ret;
  }
}
