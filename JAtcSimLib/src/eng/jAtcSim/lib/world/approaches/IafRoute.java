package eng.jAtcSim.lib.world.approaches;

import eng.eSystem.xmlSerialization.XmlOptional;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.global.PlaneCategoryDefinitions;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.world.Navaid;

public class IafRoute {
  private String iaf;
  private Navaid _navaid;
  private String route;
  private SpeechList<IAtcCommand> _routeCommands;
  @XmlOptional
  private PlaneCategoryDefinitions category = PlaneCategoryDefinitions.getAll();

  public void bind() {
    _navaid = Acc.area().getNavaids().get(iaf);
    _routeCommands = Approach.parseRoute(route);
  }

  public PlaneCategoryDefinitions getCategory() {
    return category;
  }

  public Navaid getNavaid() {
    return _navaid;
  }

  public SpeechList<IAtcCommand> getRouteCommands() {
    return _routeCommands;
  }
}
