package eng.jAtcSim.newLib.speeches.atc2airplane.afterCommands;

import eng.jAtcSim.newLib.speeches.ICommand;

public abstract class AfterCommand implements ICommand {
  private final AfterValuePosition extension;

  protected AfterCommand(AfterValuePosition extension) {
    this.extension = extension;
  }

  public AfterValuePosition getExtension() {
    return extension;
  }
}
