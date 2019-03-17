package eng.jAtcSim.lib.world.xmlModel;

import eng.eSystem.collections.*;
import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
import eng.eSystem.xmlSerialization.annotations.XmlOptional;
import eng.jAtcSim.lib.global.PlaneCategoryDefinitions;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.world.Navaid;

public class XmlIafRoute {
  public String iaf;
  public String route;
  public PlaneCategoryDefinitions category = PlaneCategoryDefinitions.getAll();
}
