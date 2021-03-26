package eng.jAtcSim.newLib.speeches.airplane.atc2airplane;

import eng.jAtcSim.newLib.speeches.airplane.ICommand;

import exml.annotations.XConstructor;

/**
 * @author Marek
 */
public class ClearedForTakeoffCommand implements ICommand {
  private final String runwayThresholdName;

  public ClearedForTakeoffCommand(String runwayThresholdName) {
    this.runwayThresholdName = runwayThresholdName;
  }

  @XConstructor

  private ClearedForTakeoffCommand() {
    runwayThresholdName = null;
  }

  public String getRunwayThresholdName() {
    return runwayThresholdName;
  }

  @Override
  public String toString() {
    String ret = "Cleared for takeoff {command}";
    return ret;
  }

}
