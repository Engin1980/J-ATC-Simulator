package eng.jAtcSim.lib.world.approaches;

import eng.eSystem.xmlSerialization.XmlOptional;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.global.KeyItem;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.world.Navaid;

public class IafRoute implements KeyItem<Navaid> {
  private String iaf;
  private Navaid _navaid;
  private String route;
  private SpeechList<IAtcCommand> _routeCommands;
  @XmlOptional
  private String category;

  @Override
  public Navaid getKey() {
    return _navaid;
  }

  public void bind() {
    _navaid = Acc.area().getNavaids().get(iaf);
    _routeCommands = Approach.parseRoute(route);
  }

  public Navaid getNavaid() {
    return _navaid;
  }

  public SpeechList<IAtcCommand> getRouteCommands() {
    return _routeCommands;
  }
}
