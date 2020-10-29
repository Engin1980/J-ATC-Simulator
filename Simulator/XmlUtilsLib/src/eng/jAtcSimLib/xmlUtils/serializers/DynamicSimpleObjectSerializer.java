//package eng.jAtcSimLib.xmlUtils.serializers;
//
//import eng.eSystem.collections.IReadOnlyList;
//import eng.eSystem.eXml.XElement;
//import eng.jAtcSimLib.xmlUtils.ObjectUtils;
//import eng.jAtcSimLib.xmlUtils.Serializer;
//import eng.jAtcSimLib.xmlUtils.XmlSaveUtils;
//
//public class DynamicSimpleObjectSerializer<T> implements Serializer<T> {
//  @Override
//  public void invoke(XElement targetElement, Object value) {
//    if (value == null)
//      XmlSaveUtils.saveNullIntoElementContent(targetElement);
//    else {
//      IReadOnlyList<String> fieldNames = ObjectUtils.getFieldNames(value.getClass());
//      XmlSaveUtils.Field.storeFields(targetElement, value, fieldNames);
//
//      XmlSaveUtils.saveIntoElementChild(targetElement, DefaultXmlNames.CLASS_NAME, value.getClass().getName());
//    }
//  }
//}
