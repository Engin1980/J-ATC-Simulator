package eng.jAtcSim.newLib.speeches.airplane.atc2airplane.afterCommands;

import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;

public class AfterRadialCommand extends AfterCommandWithNavaid {

  public static AfterRadialCommand create(String navaidName, int radial) {
    AfterRadialCommand ret = new AfterRadialCommand(navaidName, radial);
    return ret;
  }

  private final int radial;

  private AfterRadialCommand(String navaidName, int radial) {
    super(navaidName, AboveBelowExactly.exactly);
    this.radial = radial;
  }

  public int getRadial() {
    return radial;
  }
}
