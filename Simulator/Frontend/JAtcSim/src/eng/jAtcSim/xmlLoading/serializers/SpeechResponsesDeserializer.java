//package eng.jAtcSim.xmlLoading.serializers;
//
//import eng.eSystem.collections.EList;
//import eng.eSystem.collections.EMap;
//import eng.eSystem.collections.IList;
//import eng.eSystem.collections.IMap;
//import eng.eSystem.eXml.XElement;
//import eng.eSystem.exceptions.EApplicationException;
//import eng.eXmlSerialization.meta.GenericParameterXmlRuleList;
//import eng.eXmlSerialization.serializers.ElementSerializer;
//import eng.jAtcSim.newLib.textProcessing.implemented.dynamicPlaneFormatter.types.Sentence;
//
//import static eng.eSystem.utilites.FunctionShortcuts.sf;
//
//public class SpeechResponsesDeserializer extends ElementSerializer {
//  private final static String[] prefixes = {
//          "eng.jAtcSim.newLib.speeches.airplane.atc2airplane.",
//          "eng.jAtcSim.newLib.speeches.airplane.atc2airplane.afterCommands.",
//          "eng.jAtcSim.newLib.speeches.airplane.airplane2atc.",
//          "eng.jAtcSim.newLib.speeches.base.",
//          "eng.jAtcSim.newLib.speeches.airplane.airplane2atc.responses."
//  };
//
//
//  private static Class getTypeClass(String className) {
//    Class cls = null;
//    for (String prefix : prefixes) {
//      String fullName = prefix + className;
//      try {
//        cls = Class.forName(fullName);
//        break;
//      } catch (ClassNotFoundException e) {
//      }
//    }
//    if (cls == null) {
//      throw new EApplicationException(sf("Unable to find class '%s' as response application.", className));
//    }
//    return cls;
//  }
//
//  @Override
//  public boolean acceptsType(Class<?> aClass) {
//    return String.class.equals(aClass);
//  }
//
//  @Override
//  protected Object _deserialize(XElement xElement, Class<?> aClass, GenericParameterXmlRuleList genericParameterXmlRuleList) {
//    IMap<Class, IList<Sentence>> tmp = new EMap<>();
//
//    for (XElement responseElement : xElement.getChildren("response")) {
//      String type = responseElement.getAttribute("type");
//      Class cls = getTypeClass(type);
//      IList<Sentence> lst = new EList<>();
//      tmp.set(cls, lst);
//      for (XElement sentenceElement : responseElement.getChildren("sentence")) {
//        String text = sentenceElement.getContent();
//        String kind = sentenceElement.tryGetAttribute("kind");
//        Sentence sent = new Sentence(kind, text);
//        lst.add(sent);
//      }
//    }
//
//    return tmp;
//  }
//
//  @Override
//  protected void _serialize(Object o, XElement xElement, GenericParameterXmlRuleList genericParameterXmlRuleList) {
//    throw new UnsupportedOperationException();
//  }
//}
