package eng.jAtcSim.lib.world.xmlModel.approaches;

import eng.eSystem.xmlSerialization.annotations.XmlOptional;

public class XmGuidedDescentApproach extends XmlApproach {
  public int radial;
  public int initialAltitude;
  @XmlOptional
  public double slope = 3;
}