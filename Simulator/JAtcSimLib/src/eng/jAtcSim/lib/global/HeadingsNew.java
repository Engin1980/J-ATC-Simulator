package eng.jAtcSim.lib.global;

import eng.jAtcSim.lib.speaking.fromAtc.commands.ChangeHeadingCommand;

public class HeadingsNew {

  public static ChangeHeadingCommand.eDirection getBetterDirectionToTurn(double current, double target) {
    target = target - current;
    target = Headings.to(target);
    if (target > 180)
      return ChangeHeadingCommand.eDirection.left;
    else
      return ChangeHeadingCommand.eDirection.right;
  }
}
