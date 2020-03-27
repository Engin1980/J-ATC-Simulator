package eng.jAtcSim.newLib.speeches.atc2airplane.afterCommands;

public class AfterHeadingCommand extends AfterCommand {
  public static AfterHeadingCommand create(int heading, AfterValuePosition position) {
    AfterHeadingCommand ret = new AfterHeadingCommand(heading, position);
    return ret;
  }

  private final int heading;

  private AfterHeadingCommand(int heading, AfterValuePosition position) {
    super(position);
    this.heading = heading;
  }

  public int getHeading() {
    return heading;
  }
}
