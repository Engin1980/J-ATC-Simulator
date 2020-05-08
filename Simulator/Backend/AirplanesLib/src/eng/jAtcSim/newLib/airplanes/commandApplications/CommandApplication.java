package eng.jAtcSim.newLib.airplanes.commandApplications;

import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;
import eng.jAtcSim.newLib.shared.exceptions.ToDoException;
import eng.jAtcSim.newLib.speeches.Confirmation;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ICommand;
import eng.jAtcSim.newLib.speeches.Rejection;

public abstract class CommandApplication<T extends ICommand> {

  protected Rejection getIllegalNavaidRejection(String navaidName){
    throw new ToDoException();
  }

  //region Public methods
  public ConfirmationResult confirm(Airplane plane, T c, boolean checkStateSanity, boolean checkCommandSanity) {
    ConfirmationResult ret = new ConfirmationResult();
    if (checkStateSanity)
      ret.rejection = getRejectionIfAirplaneStateIsInvalid(plane.getReader().getState(), c);
    if (ret.rejection == null && checkCommandSanity) {
      ret.rejection = checkCommandSanity(plane, c);
    }
    if (ret.rejection == null)
      ret.confirmation = new Confirmation(c);
    return ret;
  }

  public ApplicationResult apply(Airplane plane, T c, boolean checkStateSanity) {
    ApplicationResult ret;
    Rejection rejection = null;
    if (checkStateSanity)
      rejection = getRejectionIfAirplaneStateIsInvalid(plane.getReader().getState(), c);
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

  protected abstract Rejection checkCommandSanity(Airplane plane, T c);

  protected abstract AirplaneState [] getInvalidStates();

  protected abstract ApplicationResult adjustAirplane(Airplane plane, T c);

  protected Rejection getRejection(ICommand c, String reason) {
    Rejection ret = new Rejection("Unable to comply the command in the current state.", c);
    return ret;
  }

  protected Rejection getUnableDueToState(ICommand c) {
    Rejection ret = getRejection(c, "Unable to comply the command in the current state.");
    return ret;
  }

  protected boolean isUnableDueToState(Airplane plane, AirplaneState... states) {
    boolean ret = plane.getReader().getState().is(states);
    return ret;
  }

  //endregion

  //region Private methods

  private Rejection getRejectionIfAirplaneStateIsInvalid(AirplaneState state, ICommand cmd){
    Rejection ret;
    AirplaneState [] invalidStates = getInvalidStates();
    if (state.is(invalidStates)) {
      ret = new Rejection("Unable to comply a command now, does not fit our state.", cmd);
    } else
      ret = null;
    return ret;
  }

  //endregion

}
