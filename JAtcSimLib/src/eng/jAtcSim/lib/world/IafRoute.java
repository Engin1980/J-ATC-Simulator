package eng.jAtcSim.lib.world;

import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.global.KeyItem;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.world.approaches.Approach;

public class IafRoute implements KeyItem<Navaid> {
  private String route;
  private SpeechList<IAtcCommand> _routeCommands = null;

  private String iaf;
  private Navaid _mainFix = null;

  @Override
  public Navaid getKey() {
    return _mainFix;
  }

  public void bind(){
    _routeCommands = Approach.parseRoute(route);
    _mainFix = Acc.area().getNavaids().get(iaf);
  }
}
