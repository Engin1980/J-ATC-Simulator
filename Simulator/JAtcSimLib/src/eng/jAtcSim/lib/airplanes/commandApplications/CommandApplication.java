package eng.jAtcSim.lib.airplanes.commandApplications;

import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.speaking.IFromAirplane;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.Confirmation;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.Rejection;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;

public abstract class CommandApplication<T extends IAtcCommand> {

  // TODO BIG one! "confirm" and "apply" should have Pilot as parameter, not Airplane
  public ConfirmationResult confirm(Airplane.Airplane4Command plane, T c, boolean checkStateSanity, boolean checkCommandSanity) {
    ConfirmationResult ret = new ConfirmationResult();
    if (checkStateSanity)
      ret.rejection = checkStateSanity(plane.getState(), c);
    if (ret.rejection == null && checkCommandSanity) {
      ret.rejection = checkCommandSanity(plane, c);
    }
    if (ret.rejection == null)
      ret.confirmation = new Confirmation(c);
    return ret;
  }

  public ApplicationResult apply(Airplane.Airplane4Command plane, T c, boolean checkStateSanity) {
    ApplicationResult ret;
    IFromAirplane rejection = null;
    if (checkStateSanity)
      rejection = checkStateSanity(plane.getState(), c);
    if (rejection == null)
      rejection = checkCommandSanity(plane, c);
    if (rejection == null) {
      ret = adjustAirplane(plane, c);
    } else {
      ret = new ApplicationResult();
      ret.rejection = rejection;
    }
    return ret;
  }

  protected abstract IFromAirplane checkCommandSanity(Airplane.Airplane4Command plane, T c);

  private IFromAirplane checkStateSanity(Airplane.State state, IAtcCommand cmd){
    IFromAirplane ret;
    Airplane.State [] invalidStates = getInvalidStates();
    if (state.is(invalidStates)) {
      ret = new Rejection("Unable to comply a command now, does not fit our state.", cmd);
    } else
      ret = null;
    return ret;
  }

  protected abstract Airplane.State [] getInvalidStates();

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
