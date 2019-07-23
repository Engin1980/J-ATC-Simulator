package eng.jAtcSim.lib.world.xmlModel.approachesOld;

import eng.eSystem.collections.IList;
import eng.jAtcSim.lib.global.PlaneCategoryDefinitions;
import eng.jAtcSim.lib.world.approaches.Approach;
import eng.jAtcSim.lib.world.xmlModel.approachesOld.approachStages.XmlStage;

public class XmlCustomApproach extends XmlApproach {
  public Approach.ApproachType type;
  public PlaneCategoryDefinitions planeCategories;
  public XmlApproachEntryLocation entryLocation;
  public IList<XmlStage> stages;
}
