package eng.jAtcSim.newLib.airplanes.commandApplications;

import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;
import eng.jAtcSim.newLib.shared.exceptions.ToDoException;
import eng.jAtcSim.newLib.speeches.airplane.ICommand;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.PlaneConfirmation;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.PlaneRejection;

public abstract class CommandApplication<T extends ICommand> {

  protected PlaneRejection getIllegalNavaidRejection(String navaidName){
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
      ret.confirmation = new PlaneConfirmation(c);
    return ret;
  }

  public ApplicationResult apply(Airplane plane, T c, boolean checkStateSanity) {
    ApplicationResult ret;
    PlaneRejection rejection = null;
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

  protected abstract PlaneRejection checkCommandSanity(Airplane plane, T c);

  protected abstract AirplaneState [] getInvalidStates();

  protected abstract ApplicationResult adjustAirplane(Airplane plane, T c);

  protected PlaneRejection getRejection(ICommand c, String reason) {
    PlaneRejection ret = new PlaneRejection(c,"Unable to comply the command in the current state.");
    return ret;
  }

  protected PlaneRejection getUnableDueToState(ICommand c) {
    PlaneRejection ret = getRejection(c, "Unable to comply the command in the current state.");
    return ret;
  }

  protected boolean isUnableDueToState(Airplane plane, AirplaneState... states) {
    boolean ret = plane.getReader().getState().is(states);
    return ret;
  }

  //endregion

  //region Private methods

  private PlaneRejection getRejectionIfAirplaneStateIsInvalid(AirplaneState state, ICommand cmd){
    PlaneRejection ret;
    AirplaneState [] invalidStates = getInvalidStates();
    if (state.is(invalidStates)) {
      ret = new PlaneRejection(cmd, "Unable to comply a command now, does not fit our state.");
    } else
      ret = null;
    return ret;
  }

  //endregion

}
