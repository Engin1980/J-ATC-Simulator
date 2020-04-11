package eng.jAtcSim.newLib.speeches.atc2airplane.afterCommands;

import eng.eSystem.validation.EAssert;

public class AfterDistanceCommand extends AfterCommandWithNavaid {
  public static AfterDistanceCommand create(String navaidName, double distance, AfterValuePosition position) {
    AfterDistanceCommand ret = new AfterDistanceCommand(navaidName, distance, position);
    return ret;
  }

  private final double distance;

  protected AfterDistanceCommand(String navaidName, double distance, AfterValuePosition position) {
    super(navaidName, position);
    EAssert.Argument.isTrue(distance >= 0);
    this.distance = distance;
  }

  public double getDistance() {
    return distance;
  }
}
