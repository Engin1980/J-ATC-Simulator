package eng.jAtcSim.newLib.world.approaches.stages;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.jAtcSim.newLib.speaking.fromAtc.IAtcCommand;

public class RouteStage implements IApproachStage {

  private final IList<IAtcCommand> route;
  private final IExitCondition exitCondition;

  public RouteStage(IList<IAtcCommand> route, IExitCondition exitCondition) {
    this.route = route;
    this.exitCondition = exitCondition;
  }

  public RouteStage(IExitCondition exitCondition, IAtcCommand ... cmds) {
    this.route = new EList<>(cmds);
    this.exitCondition = exitCondition;
  }

  public IExitCondition getExitCondition() {
    return exitCondition;
  }

  public IReadOnlyList<IAtcCommand> getRoute(){
    return this.route;
  }

}
