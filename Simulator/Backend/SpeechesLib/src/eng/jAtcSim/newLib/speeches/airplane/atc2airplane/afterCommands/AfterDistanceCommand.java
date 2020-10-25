package eng.jAtcSim.newLib.speeches.airplane.atc2airplane.afterCommands;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;

public class AfterDistanceCommand extends AfterCommandWithNavaid {
  public static AfterDistanceCommand create(String navaidName, double distance, AboveBelowExactly position) {
    AfterDistanceCommand ret = new AfterDistanceCommand(navaidName, distance, position);
    return ret;
  }

  private final double distance;

  protected AfterDistanceCommand(String navaidName, double distance, AboveBelowExactly position) {
    super(navaidName, position);
    EAssert.Argument.isTrue(distance >= 0);
    this.distance = distance;
  }

  public double getDistance() {
    return distance;
  }

  @Override
  public String toString() {
    return "AfterDistanceCommand{" +
            "navaid=" + super.getNavaidName() + "; " +
            "distance=" + super.getPosition().toString() + " " + distance +
            '}';
  }
}
