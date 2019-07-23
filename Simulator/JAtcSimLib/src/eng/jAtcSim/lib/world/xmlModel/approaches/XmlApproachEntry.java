package eng.jAtcSim.lib.world.xmlModel.approaches;

import eng.jAtcSim.lib.world.approaches.entryLocations.RegionalApproachEntryLocation;
import eng.jAtcSim.lib.world.xmlModel.approaches.entryLocations.XmlApproachEntryLocation;
import eng.jAtcSim.lib.world.xmlModel.approaches.entryLocations.XmlFixRelatedApproachEntryLocation;

import javax.xml.bind.annotation.XmlElement;

public class XmlApproachEntry {
  @XmlElement(name = "FixRelatedApproachEntryLocation", type = XmlFixRelatedApproachEntryLocation.class)
  @XmlElement(name = "RegionalApproachEntryLocation", type = RegionalApproachEntryLocation.class)
  public XmlApproachEntryLocation location;
  public String routeCommands;
}
