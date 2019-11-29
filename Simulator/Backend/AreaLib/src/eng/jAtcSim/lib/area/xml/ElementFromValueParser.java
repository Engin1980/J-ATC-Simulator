//package eng.jAtcSim.lib.eng.jAtcSim.lib.world.xml;
//
//import eng.eSystem.eXml.XElement;
//import eng.eSystem.xmlSerialization.XmlSerializer;
//import eng.eSystem.xmlSerialization.supports.IElementParser;
//import eng.eSystem.xmlSerialization.supports.IValueParser;
//
//public class ElementFromValueParser<V> implements IElementParser<V> {
//
//  private final IValueParser<V> valueParser;
//
//  public ElementFromValueParser(IValueParser<V> valueParser) {
//    if (valueParser == null) {
//        throw new IllegalArgumentException("Value of {valueParser} cannot not be null.");
//    }
//
//    this.valueParser = valueParser;
//  }
//
//  @Override
//  public V parse(XElement xElement, XmlSerializer.Deserializer deserializer) {
//    String s = xElement.getContent();
//    V ret = valueParser.parse(s);
//    return ret;
//  }
//
//  @Override
//  public void format(V v, XElement xElement, XmlSerializer.Serializer serializer) {
//String s = valueParser.format(v);
//xElement.setContent(s);
//  }
//}
