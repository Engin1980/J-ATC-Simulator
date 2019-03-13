package eng.jAtcSim.lib.world.xmlModel;

import eng.eSystem.xmlSerialization.annotations.XmlOptional;
import eng.jAtcSim.lib.atcs.Atc;

public class XmlAtcTemplate {
  public Atc.eType type;
  public String name;
  public double frequency;
  public int acceptAltitude;
  public int releaseAltitude;
  public int orderedAltitude;
  @XmlOptional
  public Integer ctrAcceptDistance = null;
  @XmlOptional
  public Integer ctrNavaidAcceptDistance = null;
}
