//package eng.jAtcSim.lib.eng.jAtcSim.lib.world.xmlModel.approachesOld;
//
//import eng.eSystem.collections.EList;
//import eng.eSystem.collections.IList;
//import eng.eSystem.xmlSerialization.annotations.XmlItemElement;
//import eng.eSystem.xmlSerialization.annotations.XmlOptional;
//import eng.jAtcSim.lib.eng.jAtcSim.lib.world.xmlModel.XmlIafRoute;
//
//public class XmlApproach {
//
//  @XmlOptional
//  public String categories = "ABCD";
//
//  public String gaRoute;
//  @XmlOptional
//  @XmlItemElement(elementName = "route", type = XmlIafRoute.class)
//  public IList<XmlIafRoute> iafRoutes = new EList<>();
//  @XmlOptional
//  public String includeIafRoutesGroups = null;
//}
