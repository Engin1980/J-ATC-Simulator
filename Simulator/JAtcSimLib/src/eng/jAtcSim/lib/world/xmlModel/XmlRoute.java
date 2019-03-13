package eng.jAtcSim.lib.world.xmlModel;

import eng.eSystem.xmlSerialization.annotations.XmlOptional;
import eng.jAtcSim.lib.global.PlaneCategoryDefinitions;
import eng.jAtcSim.lib.world.Route;

public class XmlRoute {
  public Route.eType type;
  public String name;
  public String route;
  @XmlOptional
  public PlaneCategoryDefinitions category = PlaneCategoryDefinitions.getAll();
  @XmlOptional
  public String mainFix = null;
  @XmlOptional
  public Integer entryFL = null;
}
