package eng.jAtcSim.newLib.speeches.atc2airplane.afterCommands;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;

public class AfterSpeedCommand extends AfterCommand {
  public static AfterSpeedCommand create(int speed, AboveBelowExactly position) {
    AfterSpeedCommand ret = new AfterSpeedCommand(speed, position);
    return ret;
  }

  private final int speed;

  private AfterSpeedCommand(int speed, AboveBelowExactly position) {
    super(position);
    EAssert.Argument.isTrue(speed >= 0);
    this.speed = speed;
  }


  public int getSpeed() {
    return speed;
  }
}
