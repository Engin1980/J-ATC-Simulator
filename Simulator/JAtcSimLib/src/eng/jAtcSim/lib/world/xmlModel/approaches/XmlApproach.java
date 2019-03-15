package eng.jAtcSim.lib.world.xmlModel.approaches;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.xmlSerialization.annotations.XmlItemElement;
import eng.eSystem.xmlSerialization.annotations.XmlOptional;
import eng.jAtcSim.lib.world.approaches.Approach;
import eng.jAtcSim.lib.world.approaches.IafRoute;
import eng.jAtcSim.lib.world.xmlModel.XmlIafRoute;

public class XmlApproach {
  public Approach.ApproachType type;

  @XmlOptional
  public String categories = "ABCD";

  public String gaRoute;
  @XmlOptional
  @XmlItemElement(elementName = "route", type = XmlIafRoute.class)
  public IList<IafRoute> iafRoutes = new EList<>();
  @XmlOptional
  public String includeIafRoutesGroups = null;
}
