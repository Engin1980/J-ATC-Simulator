package eng.jAtcSim.newLib.speeches.atc2airplane.afterCommands;

public class AfterRadialCommand extends AfterCommandWithNavaid {

  public static AfterRadialCommand create(String navaidName, int radial, AfterValuePosition position) {
    AfterRadialCommand ret = new AfterRadialCommand(navaidName, radial, position);
    return ret;
  }

  private final int radial;

  private AfterRadialCommand(String navaidName, int radial, AfterValuePosition position) {
    super(navaidName, position);
    this.radial = radial;
  }

  public int getRadial() {
    return radial;
  }
}
