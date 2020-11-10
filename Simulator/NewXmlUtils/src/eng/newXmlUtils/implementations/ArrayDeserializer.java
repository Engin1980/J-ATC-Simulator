package eng.newXmlUtils.implementations;

import eng.newXmlUtils.XmlContext;
import eng.newXmlUtils.utils.XmlUtils;
import eng.eSystem.collections.EList;
import eng.eSystem.eXml.XElement;

import java.lang.reflect.Array;

public class ArrayDeserializer extends ItemsDeserializer {
  @Override
  public Object invoke(XElement e, XmlContext c) {
    EList lst = (EList) super.invoke(e, c);
    Class<?> componentType = XmlUtils.loadComponentType(e);
    Object ret = Array.newInstance(componentType, lst.size());
    for (int i = 0; i < lst.size(); i++) {
      Object item = lst.get(i);
      Array.set(ret, i, item);
    }
    return ret;
  }
}
