package eng.jAtcSim.newLib.speeches.airplane.atc2airplane.afterCommands;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;
import eng.newXmlUtils.annotations.XmlConstructor;

public class AfterAltitudeCommand extends AfterCommand {

  public static AfterAltitudeCommand create(int altitude, AboveBelowExactly extension){
    AfterAltitudeCommand ret = new AfterAltitudeCommand(altitude, extension);
    return ret;
  }

  private final int altitude;

  @XmlConstructor
  private AfterAltitudeCommand() {
    super(AboveBelowExactly.exactly);
    altitude = 0;
  }

  public AfterAltitudeCommand(int altitude, AboveBelowExactly position) {
    super(position);
    EAssert.Argument.isTrue(altitude >= 0);
    this.altitude = altitude;
  }

  public int getAltitude() {
    return altitude;
  }

  @Override
  public String toString() {
    return "AfterAltitudeCommand{" +
            "position=" + super.getPosition() +
            ", altitude=" + altitude +
            '}';
  }
}
