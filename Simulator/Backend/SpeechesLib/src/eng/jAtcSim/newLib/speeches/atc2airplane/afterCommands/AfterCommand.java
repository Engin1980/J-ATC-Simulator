package eng.jAtcSim.newLib.speeches.atc2airplane.afterCommands;

import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;
import eng.jAtcSim.newLib.speeches.ICommand;

public abstract class AfterCommand implements ICommand {
  private final AboveBelowExactly position;

  protected AfterCommand(AboveBelowExactly position) {
    this.position = position;
  }

  public AboveBelowExactly getPosition() {
    return position;
  }
}
