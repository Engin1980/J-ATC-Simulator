//package eng.jAtcSim.lib.world.xmlModel;
//
//import eng.eSystem.collections.EList;
//import eng.eSystem.collections.IList;
//import eng.eSystem.xmlSerialization.annotations.XmlItemElement;
//import eng.eSystem.xmlSerialization.annotations.XmlOptional;
////import eng.jAtcSim.lib.world.xml.RunwayConfigurationParser;
//
//public class XmlAirport {
//  public static class XmlSharedRoutesGroup {
//    public String groupName;
//    @XmlOptional
//    @XmlItemElement(elementName = "route", type = XmlRoute.class)
//    public IList<XmlRoute> routes = new EList<>();
//  }
//
//  public static class XmlSharedIafRoutesGroup {
//    public String groupName;
//    @XmlOptional
//    @XmlItemElement(elementName = "route", type = XmlIafRoute.class)
//    public IList<XmlIafRoute> iafRoutes = new EList<>();
//  }
//
//  public XmlInitialPosition initialPosition;
//  @XmlItemElement(elementName = "runway", type = XmlActiveRunway.class)
//  public IList<XmlActiveRunway> runways;
//  @XmlOptional
//  @XmlItemElement(elementName = "runway", type = XmlInactiveRunway.class)
//  public IList<XmlInactiveRunway> inactiveRunways = new EList<>();
//  @XmlItemElement(elementName = "atcTemplate", type = XmlAtcTemplate.class)
//  public IList<XmlAtcTemplate> atcTemplates;
//  @XmlItemElement(elementName = "hold", type = XmlPublishedHold.class)
//  public IList<XmlPublishedHold> holds;
//  @XmlOptional
//  @XmlItemElement(elementName = "entryExitPoint", type = XmlEntryExitPoint.class)
//  public IList<XmlEntryExitPoint> entryExitPoints = new EList<>();
//  public String icao;
//  public String name;
//  public int altitude;
//  public int transitionAltitude;
//  @XmlOptional
//  public int vfrAltitude = -1;
//  public String mainAirportNavaidName;
//  public double declination;
//  public int coveredDistance;
//  @XmlOptional
////  @XmlItemElement(elementName = "configuration", type = XmlRunwayConfiguration.class, parser = RunwayConfigurationParser.class)
//  @XmlItemElement(elementName = "configuration", type = XmlRunwayConfiguration.class)
//  public IList<XmlRunwayConfiguration> runwayConfigurations = new EList<>();
//  @XmlOptional
//  @XmlItemElement(elementName = "sharedRoutesGroup", type= XmlSharedRoutesGroup.class)
//  public IList<XmlSharedRoutesGroup> sharedRoutesGroups = new EList<>();
//  @XmlOptional
//  @XmlItemElement(elementName = "sharedIafRoutesGroup", type= XmlSharedIafRoutesGroup.class)
//  public IList<XmlSharedIafRoutesGroup> sharedIafRoutesGroups = new EList<>();
//
//}
