package eng.jAtcSim.newLib.area.approaches.behaviors;

import eng.eSystem.collections.*;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.speeches.ICommand;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class FlyRouteBehavior implements IApproachBehavior {
  private final IList<ICommand> commands;

  public FlyRouteBehavior(IList<ICommand> commands) {
    EAssert.Argument.isNotNull(commands, "commands");
    this.commands = commands;
  }

  @Override
  public FlyRouteBehavior createCopy() {
    return new FlyRouteBehavior(new EList<>(commands));
  }

  public IReadOnlyList<ICommand> getCommands() {
    return commands;
  }
}
