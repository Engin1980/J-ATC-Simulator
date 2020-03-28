package eng.jAtcSim.newLib.speeches.atc2airplane.afterCommands;

import eng.jAtcSim.newLib.speeches.ICommand;

public abstract class AfterCommand implements ICommand {
  private final AfterValuePosition position;

  protected AfterCommand(AfterValuePosition position) {
    this.position = position;
  }

  public AfterValuePosition getPosition() {
    return position;
  }
}
