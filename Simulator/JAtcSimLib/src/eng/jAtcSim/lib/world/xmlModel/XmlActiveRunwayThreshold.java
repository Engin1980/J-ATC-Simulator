package eng.jAtcSim.lib.world.xmlModel;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.xmlSerialization.annotations.XmlItemElement;
import eng.eSystem.xmlSerialization.annotations.XmlOptional;
import eng.jAtcSim.lib.world.Route;
import eng.jAtcSim.lib.world.xmlModel.approachesOld.*;

public class XmlActiveRunwayThreshold {
  @XmlItemElement(elementName = "ilsApproach", type = XmlIlsApproach.class)
  @XmlItemElement(elementName = "gnssApproach", type = XmlGnssApproach.class)
  @XmlItemElement(elementName = "unpreciseApproach", type = XmlUnpreciseApproach.class)
  @XmlItemElement(elementName = "customApproach", type = XmlCustomApproach.class)
  public final IList<XmlApproach> approaches = new EList<>();
  @XmlOptional
  @XmlItemElement(elementName = "route", type = Route.class)
  public final IList<XmlRoute> routes = new EList<>();
  @XmlOptional
  public String includeRoutesGroups = null;
  public String name;
  public Coordinate coordinate;
  public int initialDepartureAltitude;
  @XmlOptional // as inactive runway do not have this
  public boolean preferred = false;
}
