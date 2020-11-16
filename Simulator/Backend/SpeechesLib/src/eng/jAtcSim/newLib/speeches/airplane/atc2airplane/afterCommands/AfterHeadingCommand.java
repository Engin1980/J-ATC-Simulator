package eng.jAtcSim.newLib.speeches.airplane.atc2airplane.afterCommands;

import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;
import eng.newXmlUtils.annotations.XmlConstructor;

public class AfterHeadingCommand extends AfterCommand {
  public static AfterHeadingCommand create(int heading, AboveBelowExactly position) {
    AfterHeadingCommand ret = new AfterHeadingCommand(heading, position);
    return ret;
  }

  @XmlConstructor
  private AfterHeadingCommand() {
    super(AboveBelowExactly.exactly);
    heading = 0;
  }

  public static AfterHeadingCommand create(int heading) {
    AfterHeadingCommand ret = new AfterHeadingCommand(heading, AboveBelowExactly.exactly);
    return ret;
  }

  private final int heading;

  private AfterHeadingCommand(int heading, AboveBelowExactly position) {
    super(position);
    this.heading = heading;
  }

  public int getHeading() {
    return heading;
  }
}
