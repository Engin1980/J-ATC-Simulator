package eng.jAtcSim.newLib.speeches.airplane.atc2airplane.afterCommands;

import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;
import eng.newXmlUtils.annotations.XmlConstructor;
import exml.annotations.XConstructor;

public class AfterRadialCommand extends AfterCommandWithNavaid {

  public static AfterRadialCommand create(String navaidName, int radial) {
    AfterRadialCommand ret = new AfterRadialCommand(navaidName, radial, AboveBelowExactly.exactly);
    return ret;
  }

  public static AfterRadialCommand create(String navaidName, int radial, AboveBelowExactly position) {
    AfterRadialCommand ret = new AfterRadialCommand(navaidName, radial, position);
    return ret;
  }

  private final int radial;

  @XConstructor
  @XmlConstructor
  private AfterRadialCommand() {
    super("?", AboveBelowExactly.exactly);
    this.radial = 0;
  }


  private AfterRadialCommand(String navaidName, int radial, AboveBelowExactly position) {
    super(navaidName, position);
    this.radial = radial;
  }

  public int getRadial() {
    return radial;
  }

  @Override
  public String toString() {
    return "AfterRadialCommand{" +
            "navaid=" + super.getNavaidName() +
            "radial=" + radial +
            '}';
  }
}
