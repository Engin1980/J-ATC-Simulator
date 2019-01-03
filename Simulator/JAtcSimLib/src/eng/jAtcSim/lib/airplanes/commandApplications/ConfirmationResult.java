package eng.jAtcSim.lib.airplanes.commandApplications;

import eng.jAtcSim.lib.speaking.IFromAirplane;

public class ConfirmationResult  {
  public IFromAirplane confirmation = null;
  public IFromAirplane rejection = null;

  private static ConfirmationResult empty = new ConfirmationResult();
  public static ConfirmationResult getEmpty(){
    return empty;
  }
}
