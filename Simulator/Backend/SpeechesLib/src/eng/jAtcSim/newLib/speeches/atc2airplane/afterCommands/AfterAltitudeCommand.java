package eng.jAtcSim.newLib.speeches.atc2airplane.afterCommands;

import eng.eSystem.validation.EAssert;

public class AfterAltitudeCommand extends AfterCommand {

  public static AfterAltitudeCommand create(int altitude, AfterValuePosition extension){
    AfterAltitudeCommand ret = new AfterAltitudeCommand(altitude, extension);
    return ret;
  }

  private final int altitude;

  public AfterAltitudeCommand(int altitude, AfterValuePosition position) {
    super(position);
    EAssert.Argument.isTrue(altitude >= 0);
    this.altitude = altitude;
  }

  public int getAltitude() {
    return altitude;
  }
}
