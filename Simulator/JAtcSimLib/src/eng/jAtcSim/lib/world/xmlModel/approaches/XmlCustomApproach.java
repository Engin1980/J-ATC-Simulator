package eng.jAtcSim.lib.world.xmlModel.approaches;

import eng.eSystem.collections.IList;
import eng.jAtcSim.lib.global.PlaneCategoryDefinitions;
import eng.jAtcSim.lib.world.newApproaches.Approach;
import eng.jAtcSim.lib.world.xmlModel.XmlApproachEntryLocation;
import eng.jAtcSim.lib.world.xmlModel.approaches.approachStages.XmlStage;

public class XmlCustomApproach extends XmlApproach {
  public Approach.ApproachType type;
  public PlaneCategoryDefinitions planeCategories;
  public XmlApproachEntryLocation entryLocation;
  public IList<XmlStage> stages;
}
