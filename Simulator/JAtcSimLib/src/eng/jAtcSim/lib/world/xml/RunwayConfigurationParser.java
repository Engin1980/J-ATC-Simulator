package eng.jAtcSim.lib.world.xml;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.xmlSerialization.XmlSerializer;
import eng.eSystem.xmlSerialization.exceptions.XmlSerializationException;
import eng.eSystem.xmlSerialization.supports.IElementParser;
import eng.jAtcSim.lib.world.RunwayConfiguration;
import eng.jAtcSim.lib.world.xmlModel.XmlRunwayConfiguration;

//public class RunwayConfigurationParser implements IElementParser<XmlRunwayConfiguration> {
//
//  @Override
//  public XmlRunwayConfiguration parse(XElement xElement, XmlSerializer.Deserializer deserializer) {
//    int windFrom = 0;
//    int windTo = 359;
//    int windSpeedFrom = 0;
//    int windSpeedTo = 999;
//    IList<RunwayConfiguration.RunwayThresholdConfiguration> arrivals = new EList<>();
//    IList<RunwayConfiguration.RunwayThresholdConfiguration> departures = new EList<>();
//
//    String tmp;
//    tmp = xElement.tryGetAttribute("windFrom");
//    if (tmp != null) windFrom = Integer.parseInt(tmp);
//    tmp = xElement.tryGetAttribute("windTo");
//    if (tmp != null) windTo = Integer.parseInt(tmp);
//    tmp = xElement.tryGetAttribute("windSpeedFrom");
//    if (tmp != null) windSpeedFrom = Integer.parseInt(tmp);
//    tmp = xElement.tryGetAttribute("windSpeedTo");
//    if (tmp != null) windSpeedTo = Integer.parseInt(tmp);
//
//    RunwayConfiguration.RunwayThresholdConfiguration rtc;
//    for (XElement elm : xElement.getChildren()) {
//      String name = elm.getAttribute("name");
//      String categories = elm.tryGetAttribute("category", "ABCD");
//      boolean primary = elm.tryGetAttribute("primary", "false").equals("true");
//      boolean showRoutes = elm.tryGetAttribute("showRoutes", "true").equals("true");
//      boolean showApproach = elm.tryGetAttribute("showApproach", "true").equals("true");
//      rtc = new RunwayConfiguration.RunwayThresholdConfiguration(name, categories, primary, showRoutes, showApproach);
//      if (elm.getName().equals("arrivals"))
//        arrivals.add(rtc);
//      else if (elm.getName().equals("departures"))
//        departures.add(rtc);
//    }
//
//    XmlRunwayConfiguration ret = new XmlRunwayConfiguration(windFrom, windTo, windSpeedFrom, windSpeedTo, arrivals, departures);
//    return ret;
//  }
//
//  @Override
//  public void format(XmlRunwayConfiguration runwayConfiguration, XElement xElement, XmlSerializer.Serializer serializer) throws XmlSerializationException {
//    throw new UnsupportedOperationException("This method is not expected to be called.");
//  }
//
//}
