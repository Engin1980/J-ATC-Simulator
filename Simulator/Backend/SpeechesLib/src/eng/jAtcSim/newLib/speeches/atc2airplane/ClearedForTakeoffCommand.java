package eng.jAtcSim.newLib.speeches.atc2airplane;

import eng.jAtcSim.newLib.speeches.ICommand;

/**
 * @author Marek
 */
public class ClearedForTakeoffCommand implements ICommand {
  private final String runwayThresholdName;

  public ClearedForTakeoffCommand(String runwayThresholdName) {
    this.runwayThresholdName = runwayThresholdName;
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
