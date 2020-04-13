package eng.jAtcSim.newLib.airplanes.commandApplications;

import eng.jAtcSim.newLib.airplanes.Airplane;
import eng.jAtcSim.newLib.shared.exceptions.ToDoException;
import eng.jAtcSim.newLib.speeches.Confirmation;
import eng.jAtcSim.newLib.speeches.ICommand;
import eng.jAtcSim.newLib.speeches.Rejection;

public abstract class CommandApplication<T extends ICommand> {

  protected Rejection getIllegalNavaidRejection(String navaidName){
    throw new ToDoException();
  }

  //region Public methods
  public ConfirmationResult confirm(IAirplaneCommand plane, T c, boolean checkStateSanity, boolean checkCommandSanity) {
    ConfirmationResult ret = new ConfirmationResult();
    if (checkStateSanity)
      ret.rejection = getRejectionIfAirplaneStateIsInvalid(plane.getState(), c);
    if (ret.rejection == null && checkCommandSanity) {
      ret.rejection = checkCommandSanity(plane, c);
    }
    if (ret.rejection == null)
      ret.confirmation = new Confirmation(c);
    return ret;
  }

  public ApplicationResult apply(IAirplaneCommand plane, T c, boolean checkStateSanity) {
    ApplicationResult ret;
    Rejection rejection = null;
    if (checkStateSanity)
      rejection = getRejectionIfAirplaneStateIsInvalid(plane.getState(), c);
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
  //endregion

  //region Protected methods

  protected abstract Rejection checkCommandSanity(IAirplaneCommand plane, T c);

  protected abstract Airplane.State [] getInvalidStates();

  protected abstract ApplicationResult adjustAirplane(IAirplaneCommand plane, T c);

  protected Rejection getRejection(ICommand c, String reason) {
    Rejection ret = new Rejection("Unable to comply the command in the current state.", c);
    return ret;
  }

  protected Rejection getUnableDueToState(ICommand c) {
    Rejection ret = getRejection(c, "Unable to comply the command in the current state.");
    return ret;
  }

  protected boolean isUnableDueToState(IAirplaneCommand plane, Airplane.State... states) {
    boolean ret = plane.getState().is(states);
    return ret;
  }

  //endregion

  //region Private methods

  private Rejection getRejectionIfAirplaneStateIsInvalid(Airplane.State state, ICommand cmd){
    Rejection ret;
    Airplane.State [] invalidStates = getInvalidStates();
    if (state.is(invalidStates)) {
      ret = new Rejection("Unable to comply a command now, does not fit our state.", cmd);
    } else
      ret = null;
    return ret;
  }

  //endregion

}
