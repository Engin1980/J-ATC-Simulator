package eng.jAtcSim.newLib.area.airplanes.commandApplications;

import eng.jAtcSim.newLib.area.speaking.IFromAirplane;

public class ConfirmationResult  {
  public IFromAirplane confirmation = null;
  public IFromAirplane rejection = null;

  private static ConfirmationResult empty = new ConfirmationResult();
  public static ConfirmationResult getEmpty(){
    return empty;
  }
}
