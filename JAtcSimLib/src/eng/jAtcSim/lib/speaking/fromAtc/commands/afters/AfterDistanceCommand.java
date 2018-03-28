package eng.jAtcSim.lib.speaking.fromAtc.commands.afters;

import eng.jAtcSim.lib.world.Navaid;

public class AfterDistanceCommand extends AfterCommand {

  private Navaid navaid;
  private double distanceInNm;

  public AfterDistanceCommand(Navaid navaid, double distanceInNm) {
    this.navaid = navaid;
    this.distanceInNm = distanceInNm;
  }

  public Navaid getNavaid() {
    return navaid;
  }

  public double getDistanceInNm() {
    return distanceInNm;
  }

  @Override
  public String toString() {
    return String.format("AD{%s/%f}", navaid.getName(), distanceInNm);
  }
}
