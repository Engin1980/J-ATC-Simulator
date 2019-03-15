package eng.jAtcSim.lib.world.xmlModel.approaches;

import eng.eSystem.collections.IList;
import eng.jAtcSim.lib.world.xmlModel.XmlApproachEntryLocation;
import eng.jAtcSim.lib.world.xmlModel.approaches.approachStages.XmlStage;

public class XmlCustomApproach extends XmlApproach {
  public XmlApproachEntryLocation entryLocation;
  public IList<XmlStage> stages;
}
