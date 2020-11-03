package eng.jAtcSimLib.xmlUtils.deserializers;

import eng.eSystem.eXml.XElement;
import eng.eSystem.functionalInterfaces.Selector;
import eng.jAtcSimLib.xmlUtils.Deserializer;
import eng.jAtcSimLib.xmlUtils.XmlUtilsException;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class ProxyDeserializer<TKey, TValue> implements Deserializer {

  private final Selector<XElement, TKey> keyFromElementSelector;
  private final Selector<TValue, TKey> keyFromValueSelector;
  private final Iterable<TValue> values;

  public ProxyDeserializer(Selector<XElement, TKey> keyFromElementSelector, Selector<TValue, TKey> keyFromValueSelector, Iterable<TValue> values) {
    this.keyFromElementSelector = keyFromElementSelector;
    this.keyFromValueSelector = keyFromValueSelector;
    this.values = values;
  }

  @Override
  public Object deserialize(XElement element) {
    TKey key = keyFromElementSelector.invoke(element);
    TValue ret = null;
    for (TValue value : values) {
      if (keyFromValueSelector.invoke(value).equals(key)){
        ret = value;
        break;
      }
    }

    if (ret == null)
      throw new XmlUtilsException(sf("Key '%s' not found in values.", key));

    return ret;
  }
}
