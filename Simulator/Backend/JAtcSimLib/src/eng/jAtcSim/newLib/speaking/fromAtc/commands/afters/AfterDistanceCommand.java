package eng.jAtcSim.newLib.speaking.fromAtc.commands.afters;

import eng.jAtcSim.newLib.world.Navaid;

public class AfterDistanceCommand extends AfterCommand {

  private Navaid navaid;
  private double distanceInNm;

  private AfterDistanceCommand() {
  }

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
