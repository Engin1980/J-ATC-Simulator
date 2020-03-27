package eng.jAtcSim.newLib.speeches.atc2airplane.afterCommands;

import eng.eSystem.validation.EAssert;

public class AfterSpeedCommand extends AfterCommand {
  public static AfterSpeedCommand create(int speed, AfterValuePosition position) {
    AfterSpeedCommand ret = new AfterSpeedCommand(speed, position);
    return ret;
  }

  private final int speed;

  private AfterSpeedCommand(int speed, AfterValuePosition position) {
    super(position);
    EAssert.Argument.isTrue(speed >= 0);
    this.speed = speed;
  }


  public int getSpeed() {
    return speed;
  }
}
