package eng.jAtcSim.newLib.speeches.airplane.atc2airplane;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.speeches.airplane.ICommand;

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

  public String getRunwayThresholdName() {
    return runwayThresholdName;
  }
}
