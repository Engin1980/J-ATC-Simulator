package eng.jAtcSimLib.xmlUtils.deserializers;

import eng.eSystem.collections.IList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.functionalInterfaces.Producer;
import eng.jAtcSimLib.xmlUtils.Deserializer;
import eng.jAtcSimLib.xmlUtils.serializers.DefaultXmlNames;

public class EntriesWithListValuesDeserializer implements Deserializer {

  private Class<?> keyType;
  private Class<?> valueType;
  private Deserializer keyDeserializer;
  private Deserializer valueDeserializer;
  private Producer<IList<?>> valueListInstanceProvider;
  private Object result

  @Override
  public Object deserialize(XElement element, Class<?> type) {
    for (XElement entryElement : element.getChildren(DefaultXmlNames.ENTRY)) {
      XElement keyElement = entryElement.getChild(DefaultXmlNames.MAP_KEY);

      Object key = keyDeserializer.deserialize(keyElement, keyType);

      XElement valuesElement = entryElement.getChild(DefaultXmlNames.VALUES_KEY);
      IList<?> valuesList = valueListInstanceProvider.invoke();
      for (XElement valueElement : valuesElement.getChildren(DefaultXmlNames.VALUE_KEY)) {
        Object value = valueDeserializer.deserialize(valueElement, valueType);
        valuesList.add(value);
      }


    }
  }
}
