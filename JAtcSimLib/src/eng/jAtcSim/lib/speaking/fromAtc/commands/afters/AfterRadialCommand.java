package eng.jAtcSim.lib.speaking.fromAtc.commands.afters;

import eng.jAtcSim.lib.world.Navaid;

public class AfterRadialCommand extends AfterCommand {

  private Navaid navaid;
  private int radial;

  public AfterRadialCommand(Navaid navaid, int radial) {
    this.navaid = navaid;
    this.radial = radial;
  }

  public Navaid getNavaid() {
    return navaid;
  }

  public int getRadial() {
    return radial;
  }

  @Override
  public String toString() {
    return String.format("AR{%s/%03d}", navaid.getName(), radial);
  }
}
