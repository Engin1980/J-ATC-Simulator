package eng.jAtcSimLib.xmlUtils.deserializers;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IMap;
import eng.eSystem.eXml.XElement;
import eng.eSystem.functionalInterfaces.Producer;
import eng.jAtcSimLib.xmlUtils.Deserializer;
import eng.jAtcSimLib.xmlUtils.XmlUtilsException;
import eng.jAtcSimLib.xmlUtils.serializers.DefaultXmlNames;

import java.util.Map;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class EntriesWithListValuesDeserializer implements Deserializer {

  private final Deserializer keyDeserializer;
  private final Deserializer valueDeserializer;
  private final Producer<IList<?>> valueListInstanceProvider;
  private final Object targetInstance;

  public EntriesWithListValuesDeserializer(Deserializer keyDeserializer, Deserializer valueDeserializer, Producer<IList<?>> valueListInstanceProvider, Object targetInstance) {
    this.keyDeserializer = keyDeserializer;
    this.valueDeserializer = valueDeserializer;
    this.valueListInstanceProvider = valueListInstanceProvider;
    this.targetInstance = targetInstance;
  }

  @Override
  public Object deserialize(XElement element) {
    for (XElement entryElement : element.getChildren(DefaultXmlNames.ENTRY)) {
      XElement keyElement = entryElement.getChild(DefaultXmlNames.MAP_KEY);

      Object key = keyDeserializer.deserialize(keyElement);

      XElement valuesElement = entryElement.getChild(DefaultXmlNames.VALUES_KEY);
      IList valuesList = valueListInstanceProvider.invoke();
      for (XElement valueElement : valuesElement.getChildren(DefaultXmlNames.VALUE_KEY)) {
        Object value = valueDeserializer.deserialize(valueElement);
        valuesList.add(value);
      }

      addEntryToTargetInstance(key, valuesList);
    }

    return targetInstance;
  }

  private void addEntryToTargetInstance(Object key, IList value) {
    if (targetInstance instanceof IMap)
      ((IMap) targetInstance).set(key, value);
    else if (targetInstance instanceof Map)
      ((Map) targetInstance).put(key, value);
    else
      throw new XmlUtilsException(sf("EntriesWithListValueDeserializer cannot handle '%s' type. Expected IMap or Map.", targetInstance.getClass()));
  }


}
