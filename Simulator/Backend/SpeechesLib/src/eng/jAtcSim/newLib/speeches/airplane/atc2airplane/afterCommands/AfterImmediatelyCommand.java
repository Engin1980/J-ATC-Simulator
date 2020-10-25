package eng.jAtcSim.newLib.speeches.airplane.atc2airplane.afterCommands;

import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;

public class AfterImmediatelyCommand extends AfterCommand {
  public AfterImmediatelyCommand() {
    super(AboveBelowExactly.exactly);
  }

  @Override
  public String toString() {
    return "AfterImmediatelyCommand";
  }
}
