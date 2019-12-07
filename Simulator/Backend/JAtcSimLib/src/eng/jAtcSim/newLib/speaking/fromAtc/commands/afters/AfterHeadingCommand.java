package eng.jAtcSim.newLib.speaking.fromAtc.commands.afters;

public class AfterHeadingCommand extends AfterCommand {
  private int heading;

  private AfterHeadingCommand() {
  }

  public AfterHeadingCommand(int heading) {
    this.heading = heading;
  }

  public int getHeading() {
    return heading;
  }

  @Override
  public String toString() {
    return String.format("AH{03d}", heading);
  }
}
