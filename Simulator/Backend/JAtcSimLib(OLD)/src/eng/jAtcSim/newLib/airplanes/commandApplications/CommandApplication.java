package eng.jAtcSim.newLib.area.airplanes.commandApplications;

import eng.jAtcSim.newLib.area.airplanes.Airplane;
import eng.jAtcSim.newLib.area.airplanes.interfaces.IAirplaneWriteSimple;
import eng.jAtcSim.newLib.area.speaking.IFromAirplane;
import eng.jAtcSim.newLib.area.speaking.fromAirplane.notifications.commandResponses.Confirmation;
import eng.jAtcSim.newLib.area.speaking.fromAirplane.notifications.commandResponses.Rejection;
import eng.jAtcSim.newLib.area.speaking.fromAtc.IAtcCommand;

public abstract class CommandApplication<T extends IAtcCommand> {

  //region Public methods
  public ConfirmationResult confirm(IAirplaneWriteSimple plane, T c, boolean checkStateSanity, boolean checkCommandSanity) {
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

  public ApplicationResult apply(IAirplaneWriteSimple plane, T c, boolean checkStateSanity) {
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
  //endregion

  //region Protected methods

  protected abstract IFromAirplane checkCommandSanity(IAirplaneWriteSimple plane, T c);

  protected abstract Airplane.State [] getInvalidStates();

  protected abstract ApplicationResult adjustAirplane(IAirplaneWriteSimple plane, T c);

  protected IFromAirplane getRejection(IAtcCommand c, String reason) {
    IFromAirplane ret = new Rejection("Unable to comply the command in the current state.", c);
    return ret;
  }

  protected IFromAirplane getUnableDueToState(IAtcCommand c) {
    IFromAirplane ret = getRejection(c, "Unable to comply the command in the current state.");
    return ret;
  }

  protected boolean isUnableDueToState(IAirplaneWriteSimple plane, IAtcCommand c, Airplane.State... states) {
    boolean ret = plane.getState().is(states);
    return ret;
  }

  //endregion

  //region Private methods

  private IFromAirplane checkStateSanity(Airplane.State state, IAtcCommand cmd){
    IFromAirplane ret;
    Airplane.State [] invalidStates = getInvalidStates();
    if (state.is(invalidStates)) {
      ret = new Rejection("Unable to comply a command now, does not fit our state.", cmd);
    } else
      ret = null;
    return ret;
  }

  //endregion

}
