//package eng.newXmlUtils.implementations;
//
//import eng.eSystem.eXml.XElement;
//import eng.newXmlUtils.XmlContext;
//import eng.newXmlUtils.base.Deserializer;
//
//public class TypedObjectDeserializer<T> implements Deserializer {
//
//  public enum FieldDeserializationType {
//    fieldInjection,
//    constructorInjection,
//    contentOfFieldInjection,
//    ignored
//  }
//
//  public static <T> TypedObjectDeserializer<T> createFor(Class<T> type) {
//    TypedObjectDeserializer<T> ret = new TypedObjectDeserializer<>(type);
//    return ret;
//  }
//
//  private final Class<T> type;
//
//  private TypedObjectDeserializer(Class<T> type) {
//    this.type = type;
//  }
//
//  @Override
//  public T invoke(XElement e, XmlContext c) {
//    return null;
//  }
//}
