package eng.jAtcSim.newLib.speeches.atc2airplane.afterCommands;

import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;

public class AfterHeadingCommand extends AfterCommand {
  public static AfterHeadingCommand create(int heading) {
    AfterHeadingCommand ret = new AfterHeadingCommand(heading);
    return ret;
  }

  private final int heading;

  private AfterHeadingCommand(int heading) {
    super(AboveBelowExactly.exactly);
    this.heading = heading;
  }

  public int getHeading() {
    return heading;
  }
}
