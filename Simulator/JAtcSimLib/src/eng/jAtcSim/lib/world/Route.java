package eng.jAtcSim.lib.world;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;

public abstract class Route {
  private final IList<IAtcCommand> routeCommands = new EList<>();
  private final IList<String> mapping = new EList<>();

  public IReadOnlyList<IAtcCommand> getRouteCommands() {
    return routeCommands;
  }

  public boolean isMappingMatch(IList<String> otherMapping) {
    return mapping.isAny(q -> otherMapping.contains(q));
  }

  public boolean isMappingMatch(String otherMapping){
    assert otherMapping != null;
    IList<String> tmp = new EList<>(otherMapping.split(";"));
    return isMappingMatch(tmp);
  }

  public Route(String mapping, IList<IAtcCommand> routeCommands) {
    this.mapping.add(mapping.split(";"));
    this.routeCommands.add(routeCommands);
  }
}
