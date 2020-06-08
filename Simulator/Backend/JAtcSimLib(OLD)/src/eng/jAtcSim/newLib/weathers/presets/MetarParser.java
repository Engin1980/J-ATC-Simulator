//package eng.jAtcSim.lib.weathers.presets;
//
//import eng.eSystem.eXml.XElement;
//import eng.eSystem.xmlSerialization.XmlSerializer;
//import eng.eSystem.xmlSerialization.supports.IElementParser;
//import eng.jAtcSim.lib.weathers.decoders.MetarDecoder;
//
//public class MetarParser implements IElementParser<PresetWeather> {
//  @Override
//  public PresetWeather parse(XElement xElement, XmlSerializer.Deserializer deserializer) {
//    String s = xElement.getContent();
//    PresetWeather ret = MetarDecoder.decode(s);
//    return ret;
//  }
//
//  @Override
//  public void format(PresetWeather presetWeather, XElement xElement, XmlSerializer.Serializer serializer) {
//    throw new UnsupportedOperationException("This element parser should not be used for formatting.");
//  }
//}
