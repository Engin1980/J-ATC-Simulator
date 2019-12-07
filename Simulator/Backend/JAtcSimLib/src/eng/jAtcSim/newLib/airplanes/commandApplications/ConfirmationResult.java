package eng.jAtcSim.newLib.airplanes.commandApplications;

import eng.jAtcSim.newLib.speaking.IFromAirplane;

public class ConfirmationResult  {
  public IFromAirplane confirmation = null;
  public IFromAirplane rejection = null;

  private static ConfirmationResult empty = new ConfirmationResult();
  public static ConfirmationResult getEmpty(){
    return empty;
  }
}
