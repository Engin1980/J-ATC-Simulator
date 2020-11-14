//package eng.jAtcSimLib.xmlUtils.serializers;
//
//import eng.eSystem.eXml.XElement;
//import eng.jAtcSimLib.xmlUtils.Formatter;
//import eng.jAtcSimLib.xmlUtils.Serializer;
//import eng.jAtcSimLib.xmlUtils.XmlSaveUtils;
//
//import java.util.Map;
//
//public class EntriesViaStringSerializer<TKey, TValue>  implements Serializer<Iterable<Map.Entry<TKey, TValue>>> {
//  private final Formatter<TKey> keyToStringFormatter;
//  private final Formatter<TValue> valueToStringFormatter;
//
//  public EntriesViaStringSerializer(Formatter<TKey> keyToStringFormatter, Formatter<TValue> valueToStringFormatter) {
//    this.keyToStringFormatter = keyToStringFormatter;
//    this.valueToStringFormatter = valueToStringFormatter;
//  }
//
//  @Override
//  public void invoke(XElement targetElement, Iterable<Map.Entry<TKey, TValue>> entries) {
//    for (Map.Entry<TKey, TValue> entry : entries) {
//      String key =  keyToStringFormatter.invoke(entry.getKey());
//      XElement keyElement = XmlSaveUtils.saveAsElement("key",key);
//      String value = valueToStringFormatter.invoke(entry.getValue());
//      XElement valueElement = XmlSaveUtils.saveAsElement("value", value);
//
//      XElement entryElement = new XElement("entry");
//      entryElement.addElement(keyElement);
//      entryElement.addElement(valueElement);
//
//      targetElement.addElement(entryElement);
//    }
//  }
//}
