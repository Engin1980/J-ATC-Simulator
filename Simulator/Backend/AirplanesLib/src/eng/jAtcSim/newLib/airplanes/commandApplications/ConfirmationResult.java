package eng.jAtcSim.newLib.airplanes.commandApplications;

import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.PlaneConfirmation;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.PlaneRejection;

public class ConfirmationResult  {
  public PlaneConfirmation confirmation = null;
  public PlaneRejection rejection = null;

  private static ConfirmationResult empty = new ConfirmationResult();
  public static ConfirmationResult getEmpty(){
    return empty;
  }
}
