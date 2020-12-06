package eng.jAtcSim.newLib.area.approaches.behaviors;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.speeches.airplane.ICommand;

public class FlyRouteBehavior implements IApproachBehavior {
  private final IList<ICommand> commands;
  private boolean applied = false;

  public FlyRouteBehavior(IList<ICommand> commands) {
    EAssert.Argument.isNotNull(commands, "commands");
    this.commands = commands;
  }

  public IReadOnlyList<ICommand> getCommands() {
    return commands;
  }

  public boolean isApplied() {
    return applied;
  }

  public void setApplied() {
    this.applied = true;
  }
}
