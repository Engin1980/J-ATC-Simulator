package eng.jAtcSim.newLib.airplanes.commandApplications;

import eng.jAtcSim.newLib.speeches.Confirmation;
import eng.jAtcSim.newLib.speeches.ICommand;
import eng.jAtcSim.newLib.speeches.Rejection;
import eng.jAtcSim.newLib.speeches.Response;

public class ConfirmationResult  {
  public Confirmation confirmation = null;
  public Rejection rejection = null;

  private static ConfirmationResult empty = new ConfirmationResult();
  public static ConfirmationResult getEmpty(){
    return empty;
  }
}
