package eng.newXmlUtils.implementations;

import eng.newXmlUtils.XmlContext;
import eng.newXmlUtils.utils.InternalXmlUtils;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.eXml.XElement;

import java.lang.reflect.Array;

public class ArraySerializer extends ItemsSerializer {
  @Override
  public void invoke(XElement element, Object value, XmlContext xmlContext) {
    IList<Object> items = new EList<>();
    for (int i = 0; i < Array.getLength(value); i++) {
      Object item = Array.get(value, i);
      items.add(item);
    }

    super.invoke(element, items, xmlContext);
    InternalXmlUtils.saveType(element, value.getClass());
    InternalXmlUtils.saveComponentType(element, value.getClass().getComponentType());
  }
}
