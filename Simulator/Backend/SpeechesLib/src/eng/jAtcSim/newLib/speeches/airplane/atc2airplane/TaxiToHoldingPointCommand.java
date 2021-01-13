package eng.jAtcSim.newLib.speeches.airplane.atc2airplane;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.speeches.airplane.ICommand;
import eng.newXmlUtils.annotations.XmlConstructor;
import exml.annotations.XConstructor;

public class TaxiToHoldingPointCommand implements ICommand {
  public static TaxiToHoldingPointCommand create(String runwayThresholdName) {
    return new TaxiToHoldingPointCommand(runwayThresholdName);
  }
  private final String runwayThresholdName;

  private TaxiToHoldingPointCommand(String runwayThresholdName) {
    EAssert.Argument.isNotNull(runwayThresholdName, "runwayThresholdName");
    EAssert.Argument.isNonemptyString(runwayThresholdName, "runwayThresholdName");
    this.runwayThresholdName = runwayThresholdName;
  }

  @XConstructor
  @XmlConstructor
  private TaxiToHoldingPointCommand() {
    runwayThresholdName = null;
  }

  public String getRunwayThresholdName() {
    return runwayThresholdName;
  }

  @Override
  public String toString() {
    return "Taxi-to-HP " + runwayThresholdName + " {planeCommand}";
  }
}
