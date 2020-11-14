package eng.newXmlUtils.implementations;

import eng.newXmlUtils.base.Deserializer;
import eng.newXmlUtils.XmlContext;
import eng.newXmlUtils.utils.InternalXmlUtils;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.ESet;
import eng.eSystem.collections.IList;
import eng.eSystem.eXml.XElement;

public class ItemsDeserializer implements Deserializer {

  @Override
  public Object invoke(XElement e, XmlContext c) {
    Class<?> type = InternalXmlUtils.loadType(e);
    IList<Object> items = new EList<>();
    for (XElement child : e.getChildren(InternalXmlUtils.ITEM)) {
      Class<?> itemType = InternalXmlUtils.loadType(child);
      Deserializer deserializer = c.sdfManager.getDeserializer(itemType);
      Object item = deserializer.invoke(child, c);
      items.add(item);
    }

    Object ret = createInstance(type);
    fillInstance(ret, items);
    return ret;
  }

  private void fillInstance(Object ret, IList<Object> items) {
    if (ret instanceof EList)
      ((EList) ret).addMany(items);
    else if (ret instanceof ESet)
      ((ESet) ret).addMany(items);
    else
      throw new UnsupportedOperationException();
  }

  private Object createInstance(Class<?> type) {
    if (EList.class.equals(type))
      return new EList<>();
    else if (ESet.class.equals(type))
      return new ESet<>();
    else
      throw new UnsupportedOperationException();
  }
}
