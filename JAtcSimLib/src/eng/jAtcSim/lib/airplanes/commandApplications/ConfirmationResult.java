package eng.jAtcSim.lib.airplanes.commandApplications;

import eng.jAtcSim.lib.speaking.IFromAirplane;

public class ConfirmationResult extends ApplicationResult {
  public IFromAirplane confirmation = null;

  private static ConfirmationResult empty = new ConfirmationResult();
  public static ConfirmationResult getEmpty(){
    return empty;
  }
}
