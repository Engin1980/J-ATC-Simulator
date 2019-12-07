package eng.jAtcSim.newLib.global;

import eng.jAtcSim.newLib.speaking.fromAtc.commands.ChangeHeadingCommand;

//TODO move to Headings class
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
