//TODEL
//package eng.jAtcSim.newLib.textProcessing.implemented.dynamicPlaneFormatter.types;
//
//import eng.eSystem.collections.*;
//import eng.eSystem.eXml.XElement;
//import eng.eSystem.exceptions.EApplicationException;
//import eng.jAtcSim.newLib.textProcessing.implemented.dynamicPlaneFormatter.DynamicPlaneFormatter;
//
//import static eng.eSystem.utilites.FunctionShortcuts.*;
//
//public class XmlFormatterLoader {
//
//  private final static String[] prefixes = {
//      "eng.jAtcSim.newLib.speeches.airplane.airplane2atc.",
//      "eng.jAtcSim.newLib.speeches.airplane.airplane2atc.responses",
//      "eng.jAtcSim.newLib.speeches.airplane.atc2airplane.",
//      "eng.jAtcSim.newLib.speeches.airplane.atc2airplane.afterCommands"
//  };
//
////   "eng.jAtcSim.lib.speaking.fromAirplane.notifications.",
////       "eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.",
////       "eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.rejections.",
////       "eng.jAtcSim.lib.speaking.fromAtc.commands.",
////       "eng.jAtcSim.lib.speaking.fromAtc.commands.afters.",
////       "eng.jAtcSim.lib.speaking.fromAtc.notifications."
//
//  private static Class<?> getTypeClass(String className) {
//    Class<?> cls = null;
//    for (String prefix : prefixes) {
//      String fullName = prefix + className;
//      try {
//        cls = Class.forName(fullName);
//        break;
//      } catch (ClassNotFoundException e) {
//        // intentionally blank
//      }
//    }
//    if (cls == null) {
//      throw new EApplicationException(sf("Unable to find class '%s' as response application.", className));
//    }
//    return cls;
//  }
//
//  public DynamicPlaneFormatter parse(XElement xElement) {
//    IMap<Class<?>, IList<Sentence>> tmp = new EMap<>();
//
//    for (XElement responseElement : xElement.getChildren("response")) {
//      String type = responseElement.getAttribute("kind");
//      Class<?> cls = getTypeClass(type);
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
//    DynamicPlaneFormatter ret = new DynamicPlaneFormatter(tmp);
//    return ret;
//  }
//}
