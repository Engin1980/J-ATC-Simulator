package eng.newXmlUtils.implementations;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.ESet;
import eng.eSystem.collections.IList;
import eng.eSystem.eXml.XElement;
import eng.newXmlUtils.EXmlException;
import eng.newXmlUtils.XmlContext;
import eng.newXmlUtils.base.Deserializer;
import eng.newXmlUtils.base.InstanceFactory;
import eng.newXmlUtils.utils.InternalObjectUtils;
import eng.newXmlUtils.utils.InternalXmlUtils;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class ItemsDeserializer implements Deserializer {

  private InstanceFactory<?> instanceFactory = null;

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

    Object ret = createInstance(type, c);
    fillInstance(ret, items);
    return ret;
  }

  public ItemsDeserializer withInstanceFactory(InstanceFactory<?> instanceFactory) {
    this.instanceFactory = instanceFactory;
    return this;
  }

  private void fillInstance(Object ret, IList<Object> items) {
    if (ret instanceof EList)
      ((EList) ret).addMany(items);
    else if (ret instanceof ESet)
      ((ESet) ret).addMany(items);
    else
      throw new UnsupportedOperationException(sf("ItemsDeserializer unable to fill object of type '%s'.", ret.getClass()));
  }

  private Object createInstance(Class<?> type, XmlContext c) {
    Object ret;
    if (EList.class.equals(type))
      ret = new EList<>();
    else if (ESet.class.equals(type))
      ret = new ESet<>();
    else {
      InstanceFactory<?> f = this.instanceFactory;
      if (f == null)
        f = InternalObjectUtils.tryGetPublicConstructorFactory(type);
      if (f == null)
        f = InternalObjectUtils.tryGetAnnotatedConstructorFactory(type);
      if (f == null)
        throw new EXmlException(sf("Failed to create an instance of '%s'. Don't know how.", type));
      ret = f.invoke(c);
    }
    return ret;
  }
}
