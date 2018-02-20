package eng.jAtcSim.lib.airplanes.commandApplications;

import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.speaking.IFromAirplane;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.Confirmation;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.Rejection;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;

public abstract class CommandApplication<T extends IAtcCommand> {

  public ConfirmationResult confirm(Airplane.Airplane4Command plane, T c, boolean checkSanity) {
    ConfirmationResult ret = new ConfirmationResult();
    if (checkSanity) {
      ret.rejection = checkSanity(plane, c);
    }
    if (ret.rejection == null)
      ret.confirmation = new Confirmation(c);
    return ret;
  }

  public ApplicationResult apply(Airplane.Airplane4Command plane, T c) {
    ApplicationResult ret;
    IFromAirplane rejection = null;
    rejection = checkSanity(plane, c);
    if (rejection == null) {
      ret = adjustAirplane(plane, c);
    } else {
      ret = new ApplicationResult();
      ret.rejection = rejection;
    }
    return ret;
  }

  protected IFromAirplane checkValidState(Airplane.Airplane4Command plane, IAtcCommand c, Airplane.State... states) {
    IFromAirplane ret;
    if (plane.getState().is(states)) {
      ret = new Rejection("Unable to comply a command now.", c);
    } else
      ret = null;
    return ret;
  }

  protected abstract IFromAirplane checkSanity(Airplane.Airplane4Command plane, T c);

  protected abstract ApplicationResult adjustAirplane(Airplane.Airplane4Command plane, T c);

  protected IFromAirplane getRejection(IAtcCommand c, String reason) {
    IFromAirplane ret = new Rejection("Unable to comply the command in the current state.", c);
    return ret;
  }

  protected IFromAirplane getUnableDueToState(IAtcCommand c) {
    IFromAirplane ret = getRejection(c, "Unable to comply the command in the current state.");
    return ret;
  }

  protected boolean isUnableDueToState(Airplane.Airplane4Command plane, IAtcCommand c, Airplane.State... states) {
    boolean ret = plane.getState().is(states);
    return ret;
  }

}
